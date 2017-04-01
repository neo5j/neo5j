/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo5j.
 *
 * Neo5j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo5j.unsafe.impl.batchimport.store;

import java.io.File;
import java.io.IOException;
import java.nio.file.OpenOption;

import org.neo5j.helpers.Service;
import org.neo5j.io.fs.FileSystemAbstraction;
import org.neo5j.io.pagecache.PageCache;
import org.neo5j.io.pagecache.tracing.DefaultPageCacheTracer;
import org.neo5j.io.pagecache.tracing.PageCacheTracer;
import org.neo5j.io.pagecache.tracing.cursor.DefaultPageCursorTracerSupplier;
import org.neo5j.io.pagecache.tracing.cursor.PageCursorTracerSupplier;
import org.neo5j.kernel.api.labelscan.LabelScanStore;
import org.neo5j.kernel.configuration.Config;
import org.neo5j.kernel.extension.KernelExtensionFactory;
import org.neo5j.kernel.extension.KernelExtensions;
import org.neo5j.kernel.extension.UnsatisfiedDependencyStrategies;
import org.neo5j.kernel.extension.dependency.NamedLabelScanStoreSelectionStrategy;
import org.neo5j.kernel.impl.api.index.IndexStoreView;
import org.neo5j.kernel.impl.api.scan.LabelScanStoreProvider;
import org.neo5j.kernel.impl.factory.DatabaseInfo;
import org.neo5j.kernel.impl.logging.LogService;
import org.neo5j.kernel.impl.pagecache.ConfiguringPageCacheFactory;
import org.neo5j.kernel.impl.spi.KernelContext;
import org.neo5j.kernel.impl.spi.SimpleKernelContext;
import org.neo5j.kernel.impl.store.NeoStores;
import org.neo5j.kernel.impl.store.NodeStore;
import org.neo5j.kernel.impl.store.PropertyStore;
import org.neo5j.kernel.impl.store.RecordStore;
import org.neo5j.kernel.impl.store.RelationshipStore;
import org.neo5j.kernel.impl.store.StoreFactory;
import org.neo5j.kernel.impl.store.UnderlyingStorageException;
import org.neo5j.kernel.impl.store.counts.CountsTracker;
import org.neo5j.kernel.impl.store.format.RecordFormats;
import org.neo5j.kernel.impl.store.record.RelationshipGroupRecord;
import org.neo5j.kernel.impl.util.Dependencies;
import org.neo5j.kernel.lifecycle.LifeSupport;
import org.neo5j.logging.LogProvider;
import org.neo5j.unsafe.impl.batchimport.AdditionalInitialIds;
import org.neo5j.unsafe.impl.batchimport.Configuration;
import org.neo5j.unsafe.impl.batchimport.store.BatchingTokenRepository.BatchingLabelTokenRepository;
import org.neo5j.unsafe.impl.batchimport.store.BatchingTokenRepository.BatchingPropertyKeyTokenRepository;
import org.neo5j.unsafe.impl.batchimport.store.BatchingTokenRepository.BatchingRelationshipTypeTokenRepository;
import org.neo5j.unsafe.impl.batchimport.store.io.IoTracer;

import static java.lang.String.valueOf;
import static java.nio.file.StandardOpenOption.DELETE_ON_CLOSE;

import static org.neo5j.graphdb.factory.GraphDatabaseSettings.dense_node_threshold;
import static org.neo5j.graphdb.factory.GraphDatabaseSettings.pagecache_memory;
import static org.neo5j.helpers.collection.MapUtil.stringMap;
import static org.neo5j.kernel.impl.store.MetaDataStore.DEFAULT_NAME;
import static org.neo5j.kernel.impl.store.StoreType.RELATIONSHIP_GROUP;
import static org.neo5j.kernel.impl.transaction.log.TransactionIdStore.BASE_TX_COMMIT_TIMESTAMP;

/**
 * Creator and accessor of {@link NeoStores} with some logic to provide very batch friendly services to the
 * {@link NeoStores} when instantiating it. Different services for specific purposes.
 */
public class BatchingNeoStores implements AutoCloseable
{
    private final FileSystemAbstraction fileSystem;
    private final BatchingPropertyKeyTokenRepository propertyKeyRepository;
    private final BatchingLabelTokenRepository labelRepository;
    private final BatchingRelationshipTypeTokenRepository relationshipTypeRepository;
    private final LogProvider logProvider;
    private final File storeDir;
    private final Config neo5jConfig;
    private final PageCache pageCache;
    private final NeoStores neoStores;
    private final LifeSupport life = new LifeSupport();
    private final LabelScanStore labelScanStore;
    private final IoTracer ioTracer;
    private final RecordFormats recordFormats;

    // Some stores are considered temporary during the import and will be reordered/restructured
    // into the main store. These temporary stores will live here
    private final NeoStores temporaryNeoStores;
    private final boolean externalPageCache;

    private BatchingNeoStores( FileSystemAbstraction fileSystem, PageCache pageCache, File storeDir,
            RecordFormats recordFormats, Config neo5jConfig, LogService logService, AdditionalInitialIds initialIds,
            boolean externalPageCache, IoTracer ioTracer )
    {
        this.fileSystem = fileSystem;
        this.recordFormats = recordFormats;
        this.logProvider = logService.getInternalLogProvider();
        this.storeDir = storeDir;
        this.neo5jConfig = neo5jConfig;
        this.pageCache = pageCache;
        this.ioTracer = ioTracer;
        this.externalPageCache = externalPageCache;
        this.neoStores = newStoreFactory( DEFAULT_NAME ).openAllNeoStores( true );
        if ( alreadyContainsData( neoStores ) )
        {
            neoStores.close();
            IllegalStateException ise =
                    new IllegalStateException( storeDir + " already contains data, cannot do import here" );
            if ( !externalPageCache )
            {
                try
                {
                    pageCache.close();
                }
                catch ( Exception e )
                {
                    // Oddly enough we can't close the page cache, how to communicate this? Here we add as suppressed
                    ise.addSuppressed( e );
                }
            }
            throw ise;
        }
        try
        {
            neoStores.rebuildCountStoreIfNeeded();
        }
        catch ( IOException e )
        {
            throw new UnderlyingStorageException( e );
        }
        neoStores.getMetaDataStore().setLastCommittedAndClosedTransactionId(
                initialIds.lastCommittedTransactionId(), initialIds.lastCommittedTransactionChecksum(),
                BASE_TX_COMMIT_TIMESTAMP, initialIds.lastCommittedTransactionLogByteOffset(),
                initialIds.lastCommittedTransactionLogVersion() );
        this.propertyKeyRepository = new BatchingPropertyKeyTokenRepository(
                neoStores.getPropertyKeyTokenStore() );
        this.labelRepository = new BatchingLabelTokenRepository(
                neoStores.getLabelTokenStore() );
        this.relationshipTypeRepository = new BatchingRelationshipTypeTokenRepository(
                neoStores.getRelationshipTypeTokenStore() );

        // Instantiate the temporary stores
        temporaryNeoStores = newStoreFactory(
                "temp." + DEFAULT_NAME, DELETE_ON_CLOSE ).openNeoStores( true, RELATIONSHIP_GROUP );

        // Initialize kernel extensions
        Dependencies dependencies = new Dependencies();
        dependencies.satisfyDependency( this.neo5jConfig );
        dependencies.satisfyDependency( fileSystem );
        dependencies.satisfyDependency( this );
        dependencies.satisfyDependency( logService );
        dependencies.satisfyDependency( IndexStoreView.EMPTY );
        dependencies.satisfyDependency( pageCache );
        KernelContext kernelContext = new SimpleKernelContext( storeDir, DatabaseInfo.UNKNOWN, dependencies );
        @SuppressWarnings( { "unchecked", "rawtypes" } )
        KernelExtensions extensions = life.add( new KernelExtensions(
                kernelContext, (Iterable) Service.load( KernelExtensionFactory.class ),
                dependencies, UnsatisfiedDependencyStrategies.ignore() ) );
        life.start();
        labelScanStore = life.add( extensions.resolveDependency( LabelScanStoreProvider.class,
                new NamedLabelScanStoreSelectionStrategy( neo5jConfig ) ).getLabelScanStore() );
    }

    public static BatchingNeoStores batchingNeoStores( FileSystemAbstraction fileSystem, File storeDir,
            RecordFormats recordFormats, Configuration config, LogService logService, AdditionalInitialIds initialIds,
            Config dbConfig )
    {
        Config neo5jConfig = getNeo5jConfig( config, dbConfig );
        final PageCacheTracer tracer = new DefaultPageCacheTracer();
        PageCache pageCache = createPageCache( fileSystem, neo5jConfig, logService.getInternalLogProvider(), tracer,
                DefaultPageCursorTracerSupplier.INSTANCE );

        BatchingNeoStores batchingNeoStores =
                new BatchingNeoStores( fileSystem, pageCache, storeDir, recordFormats, neo5jConfig, logService,
                        initialIds, false, tracer::bytesWritten );
        return batchingNeoStores;
    }

    public static BatchingNeoStores batchingNeoStoresWithExternalPageCache( FileSystemAbstraction fileSystem,
            PageCache pageCache, PageCacheTracer tracer, File storeDir, RecordFormats recordFormats,
            Configuration config, LogService logService, AdditionalInitialIds initialIds, Config dbConfig )
    {
        Config neo5jConfig = getNeo5jConfig( config, dbConfig );

        BatchingNeoStores batchingNeoStores =
                new BatchingNeoStores( fileSystem, pageCache, storeDir, recordFormats, neo5jConfig, logService,
                        initialIds, true, tracer::bytesWritten );
        return batchingNeoStores;
    }

    protected static Config getNeo5jConfig( Configuration config, Config dbConfig )
    {
        return dbConfig.with( stringMap(
                dense_node_threshold.name(), valueOf( config.denseNodeThreshold() ),
                pagecache_memory.name(), valueOf( config.pageCacheMemory() ) ) );
    }

    private static PageCache createPageCache( FileSystemAbstraction fileSystem, Config config, LogProvider log,
            PageCacheTracer tracer, PageCursorTracerSupplier cursorTracerSupplier )
    {
        return new ConfiguringPageCacheFactory( fileSystem, config, tracer, cursorTracerSupplier,
                log.getLog( BatchingNeoStores.class ) ).getOrCreatePageCache();
    }

    private boolean alreadyContainsData( NeoStores neoStores )
    {
        return neoStores.getNodeStore().getHighId() > 0 || neoStores.getRelationshipStore().getHighId() > 0;
    }

        private StoreFactory newStoreFactory( String name, OpenOption... openOptions )
    {
        return new StoreFactory( storeDir, name, neo5jConfig,
                new BatchingIdGeneratorFactory( fileSystem ), pageCache, fileSystem, recordFormats, logProvider,
                openOptions );
    }

    /**
     * @return temporary relationship group store which will be deleted in {@link #close()}.
     */
    public RecordStore<RelationshipGroupRecord> getTemporaryRelationshipGroupStore()
    {
        return temporaryNeoStores.getRelationshipGroupStore();
    }

    public IoTracer getIoTracer()
    {
        return ioTracer;
    }

    public NodeStore getNodeStore()
    {
        return neoStores.getNodeStore();
    }

    public PropertyStore getPropertyStore()
    {
        return neoStores.getPropertyStore();
    }

    public BatchingPropertyKeyTokenRepository getPropertyKeyRepository()
    {
        return propertyKeyRepository;
    }

    public BatchingLabelTokenRepository getLabelRepository()
    {
        return labelRepository;
    }

    public BatchingRelationshipTypeTokenRepository getRelationshipTypeRepository()
    {
        return relationshipTypeRepository;
    }

    public RelationshipStore getRelationshipStore()
    {
        return neoStores.getRelationshipStore();
    }

    public RecordStore<RelationshipGroupRecord> getRelationshipGroupStore()
    {
        return neoStores.getRelationshipGroupStore();
    }

    public CountsTracker getCountsStore()
    {
        return neoStores.getCounts();
    }

    @Override
    public void close() throws IOException
    {
        // Flush out all pending changes
        propertyKeyRepository.close();
        labelRepository.close();
        relationshipTypeRepository.close();

        // Close the neo store
        life.shutdown();
        neoStores.close();
        // These temporary stores are configured to be deleted when closed
        temporaryNeoStores.close();
        if ( !externalPageCache )
        {
            pageCache.close();
        }
    }

    public long getLastCommittedTransactionId()
    {
        return neoStores.getMetaDataStore().getLastCommittedTransactionId();
    }

    public LabelScanStore getLabelScanStore()
    {
        return labelScanStore;
    }

    public NeoStores getNeoStores()
    {
        return neoStores;
    }
}