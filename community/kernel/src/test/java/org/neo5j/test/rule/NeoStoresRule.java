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
package org.neo5j.test.rule;

import java.io.File;
import java.io.IOException;

import org.neo5j.graphdb.mockfs.EphemeralFileSystemAbstraction;
import org.neo5j.io.fs.FileSystemAbstraction;
import org.neo5j.io.pagecache.PageCache;
import org.neo5j.io.pagecache.tracing.cursor.PageCursorTracerSupplier;
import org.neo5j.kernel.configuration.Config;
import org.neo5j.kernel.impl.pagecache.ConfiguringPageCacheFactory;
import org.neo5j.kernel.impl.store.NeoStores;
import org.neo5j.kernel.impl.store.StoreFactory;
import org.neo5j.kernel.impl.store.StoreType;
import org.neo5j.kernel.impl.store.format.RecordFormatSelector;
import org.neo5j.kernel.impl.store.format.RecordFormats;
import org.neo5j.kernel.impl.store.id.DefaultIdGeneratorFactory;
import org.neo5j.logging.Log;
import org.neo5j.logging.NullLog;
import org.neo5j.logging.NullLogProvider;

import static org.neo5j.helpers.collection.MapUtil.stringMap;
import static org.neo5j.io.pagecache.tracing.PageCacheTracer.NULL;

/**
 * Rule for opening a {@link NeoStores}, either via {@link #open(String...)}, which just uses an in-memory
 * file system, or via {@link #open(FileSystemAbstraction, PageCache, RecordFormats, String...)} which is suitable in an
 * environment where you already have an fs and page cache available.
 */
public class NeoStoresRule extends ExternalResource
{
    private final Class<?> testClass;
    private NeoStores neoStores;
    private EphemeralFileSystemAbstraction efs;
    private PageCache pageCache;
    private final StoreType[] stores;

    public NeoStoresRule( Class<?> testClass, StoreType... stores )
    {
        this.testClass = testClass;
        this.stores = stores;
    }

    public NeoStores open( String... config ) throws IOException
    {
        Config configuration = Config.embeddedDefaults( stringMap( config ) );
        RecordFormats formats = RecordFormatSelector.selectForConfig( configuration, NullLogProvider.getInstance() );
        return open( formats, config );
    }

    public NeoStores open( RecordFormats format, String... config ) throws IOException
    {
        efs = new EphemeralFileSystemAbstraction();
        Config conf = Config.embeddedDefaults( stringMap( config ) );
        pageCache = getOrCreatePageCache( conf, efs );
        return open( efs, pageCache, format, config );
    }

    public NeoStores open( FileSystemAbstraction fs, PageCache pageCache, RecordFormats format, String... config )
            throws IOException
    {
        assert neoStores == null : "Already opened";
        TestDirectory testDirectory = TestDirectory.testDirectory( testClass, fs );
        File storeDir = testDirectory.makeGraphDbDir();
        Config configuration = Config.embeddedDefaults( stringMap( config ) );
        StoreFactory storeFactory = new StoreFactory( storeDir, configuration, new DefaultIdGeneratorFactory( fs ),
                pageCache, fs, format, NullLogProvider.getInstance() );
        return neoStores = stores.length == 0
                ? storeFactory.openAllNeoStores( true )
                : storeFactory.openNeoStores( true, stores );
    }

    @Override
    protected void after( boolean successful ) throws Throwable
    {
        if ( neoStores != null )
        {
            neoStores.close();
        }
        if ( pageCache != null )
        {
            pageCache.close();
        }
        if ( efs != null )
        {
            efs.close();
        }
    }

    private static PageCache getOrCreatePageCache( Config config, FileSystemAbstraction fs )
    {
        Log log = NullLog.getInstance();
        ConfiguringPageCacheFactory pageCacheFactory = new ConfiguringPageCacheFactory( fs, config, NULL,
                PageCursorTracerSupplier.NULL, log );
        return pageCacheFactory.getOrCreatePageCache();
    }
}