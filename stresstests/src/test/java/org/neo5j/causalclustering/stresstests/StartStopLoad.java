/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo5j.
 *
 * Neo5j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo5j.causalclustering.stresstests;

import java.io.File;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import org.neo5j.causalclustering.catchup.storecopy.CopiedStoreRecovery;
import org.neo5j.causalclustering.catchup.storecopy.TemporaryStoreDirectory;
import org.neo5j.causalclustering.discovery.Cluster;
import org.neo5j.causalclustering.discovery.ClusterMember;
import org.neo5j.consistency.ConsistencyCheckService;
import org.neo5j.io.fs.FileSystemAbstraction;
import org.neo5j.io.pagecache.PageCache;
import org.neo5j.io.pagecache.impl.muninn.StandalonePageCacheFactory;
import org.neo5j.kernel.configuration.Config;
import org.neo5j.kernel.extension.KernelExtensions;

import static org.neo5j.consistency.ConsistencyCheckTool.runConsistencyCheckTool;

class StartStopLoad extends RepeatUntilOnSelectedMemberCallable
{
    private final FileSystemAbstraction fs;
    private final PageCache pageCache;

    StartStopLoad( FileSystemAbstraction fs, PageCache pageCache, BooleanSupplier keepGoing, Runnable onFailure,
            Cluster cluster, int numberOfCores, int numberOfEdges )
    {
        super( keepGoing, onFailure, cluster, numberOfCores, numberOfEdges );
        this.fs = fs;
        this.pageCache = pageCache;
    }

    @Override
    protected void doWorkOnMember( boolean isCore, int id )
    {
        ClusterMember member = isCore ? cluster.getCoreMemberById( id ) : cluster.getReadReplicaById( id );
        String storeDir = member.database().getStoreDir();
        KernelExtensions kernelExtensions =
                member.database().getDependencyResolver().resolveDependency( KernelExtensions.class );
        member.shutdown();
        assertStoreConsistent( storeDir, kernelExtensions );
        LockSupport.parkNanos( 5_000_000_000L );
        member.start();
    }

    private void assertStoreConsistent( String storeDir, KernelExtensions kernelExtensions )
    {
        File fromDirectory = new File( storeDir );
        File parent = fromDirectory.getParentFile();
        try ( TemporaryStoreDirectory storeDirectory = new TemporaryStoreDirectory( fs, pageCache, parent );
              PageCache pageCache = StandalonePageCacheFactory.createPageCache( fs ) )
        {
            fs.copyRecursively( fromDirectory, storeDirectory.storeDir() );
            new CopiedStoreRecovery( Config.defaults(), kernelExtensions.listFactories(),  pageCache )
                    .recoverCopiedStore( storeDirectory.storeDir() );
            ConsistencyCheckService.Result result = runConsistencyCheckTool( new String[]{storeDir} );
            if ( !result.isSuccessful() )
            {
                throw new RuntimeException( "Not consistent database in " + storeDir );
            }
        }
        catch ( Throwable e )
        {
            throw new RuntimeException( "Failed to run CC on " + storeDir, e );
        }
    }
}
