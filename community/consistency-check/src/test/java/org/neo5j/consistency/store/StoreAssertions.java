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
package org.neo5j.consistency.store;

import java.io.File;
import java.io.IOException;

import org.neo5j.consistency.ConsistencyCheckService;
import org.neo5j.consistency.ConsistencyCheckSettings;
import org.neo5j.consistency.checking.full.ConsistencyCheckIncompleteException;
import org.neo5j.graphdb.factory.GraphDatabaseSettings;
import org.neo5j.helpers.progress.ProgressMonitorFactory;
import org.neo5j.kernel.configuration.Config;
import org.neo5j.logging.AssertableLogProvider;
import org.neo5j.logging.NullLogProvider;

import static org.junit.Assert.assertTrue;
import static org.neo5j.helpers.collection.MapUtil.stringMap;

public class StoreAssertions
{
    private StoreAssertions()
    {
    }

    public static void assertConsistentStore( File storeDir ) throws ConsistencyCheckIncompleteException, IOException
    {
        Config configuration = Config.embeddedDefaults( stringMap( GraphDatabaseSettings.pagecache_memory.name(), "8m" ) );
        AssertableLogProvider logger = new AssertableLogProvider();
        ConsistencyCheckService.Result result = new ConsistencyCheckService().runFullConsistencyCheck(
                storeDir, configuration, ProgressMonitorFactory.NONE, NullLogProvider.getInstance(), false );

        assertTrue( "Consistency check for " + storeDir + " found inconsistencies:\n\n" + logger.serialize(),
                result.isSuccessful() );
    }
}
