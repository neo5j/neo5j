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
package org.neo5j.unsafe.impl.batchimport.store;

import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import org.neo5j.helpers.collection.MapUtil;
import org.neo5j.io.fs.FileSystemAbstraction;
import org.neo5j.kernel.configuration.Config;
import org.neo5j.kernel.impl.logging.SimpleLogService;
import org.neo5j.kernel.impl.store.format.RecordFormatSelector;
import org.neo5j.logging.AssertableLogProvider;
import org.neo5j.metrics.MetricsExtension;
import org.neo5j.metrics.MetricsSettings;
import org.neo5j.test.rule.TestDirectory;
import org.neo5j.test.rule.fs.DefaultFileSystemRule;
import org.neo5j.unsafe.impl.batchimport.AdditionalInitialIds;
import org.neo5j.unsafe.impl.batchimport.Configuration;

public class BatchingNeoStoresIT
{
    @Rule
    public TestDirectory testDirectory = TestDirectory.testDirectory();
    @Rule
    public DefaultFileSystemRule fileSystemRule = new DefaultFileSystemRule();

    @Test
    public void startBatchingNeoStoreWithMetricsPluginEnabled() throws Exception
    {
        FileSystemAbstraction fileSystem = fileSystemRule.get();
        File storeDir = testDirectory.graphDbDir();
        Config config = Config.defaults()
                .with( MapUtil.stringMap( MetricsSettings.metricsEnabled.name(), "true" ) );
        AssertableLogProvider provider = new AssertableLogProvider();
        SimpleLogService logService = new SimpleLogService( provider, provider );

        try ( BatchingNeoStores batchingNeoStores = BatchingNeoStores
                .batchingNeoStores( fileSystem, storeDir, RecordFormatSelector.defaultFormat(), Configuration.DEFAULT,
                        logService, AdditionalInitialIds.EMPTY, config ) )
        {
            // empty block
        }
        provider.assertNone( AssertableLogProvider.inLog( MetricsExtension.class ).any() );
    }
}