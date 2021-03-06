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
package org.neo5j.dbms;

import java.io.File;

import org.neo5j.configuration.Description;
import org.neo5j.configuration.LoadableConfig;
import org.neo5j.graphdb.config.Setting;
import org.neo5j.configuration.Internal;

import static org.neo5j.kernel.configuration.Settings.PATH;
import static org.neo5j.kernel.configuration.Settings.STRING;
import static org.neo5j.kernel.configuration.Settings.derivedSetting;
import static org.neo5j.kernel.configuration.Settings.pathSetting;
import static org.neo5j.kernel.configuration.Settings.setting;

public class DatabaseManagementSystemSettings implements LoadableConfig
{
    @Description( "Name of the database to load" )
    public static final Setting<String> active_database = setting( "dbms.active_database", STRING, "graph.db" );

    @Description( "Path of the data directory. You must not configure more than one Neo5j installation to use the " +
            "same data directory." )
    public static final Setting<File> data_directory = pathSetting( "dbms.directories.data", "data" );

    @Internal
    public static final Setting<File> database_path = derivedSetting( "unsupported.dbms.directories.database",
            data_directory, active_database,
            ( data, current ) -> new File( new File( data, "databases" ), current ),
            PATH );

    @Internal
    public static final Setting<File> auth_store_directory = derivedSetting( "unsupported.dbms.directories.auth",
            data_directory,
            ( data ) -> new File( data, "dbms" ),
            PATH );
}
