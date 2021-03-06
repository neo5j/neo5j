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
package org.neo5j.kernel.api.impl.schema;

import java.io.File;

import org.neo5j.io.fs.FileSystemAbstraction;
import org.neo5j.kernel.api.impl.index.storage.DirectoryFactory;
import org.neo5j.kernel.api.index.IndexProviderCompatibilityTestSuite;
import org.neo5j.kernel.configuration.Config;
import org.neo5j.kernel.impl.factory.OperationalMode;
import org.neo5j.logging.NullLogProvider;

public class LuceneSchemaIndexProviderCompatibilitySuiteTest extends IndexProviderCompatibilityTestSuite
{
    @Override
    protected LuceneSchemaIndexProvider createIndexProvider( FileSystemAbstraction fs, File graphDbDir )
    {
        DirectoryFactory.InMemoryDirectoryFactory directoryFactory = new DirectoryFactory.InMemoryDirectoryFactory();
        NullLogProvider logging = NullLogProvider.getInstance();
        Config config = Config.defaults();
        OperationalMode mode = OperationalMode.single;
        return new LuceneSchemaIndexProvider( fs, directoryFactory, graphDbDir, logging, config, mode );

    }
}
