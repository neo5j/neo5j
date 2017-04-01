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
package org.neo5j.server.enterprise;

import java.io.IOException;

import org.neo5j.server.BlockingBootstrapper;
import org.neo5j.server.Bootstrapper;
import org.neo5j.server.ServerBootstrapper;
import org.neo5j.server.ServerCommandLineArgs;

import static org.neo5j.commandline.Util.neo5jVersion;

public class ArbiterEntryPoint
{
    private static Bootstrapper bootstrapper;

    public static void main( String[] argv ) throws IOException
    {
        ServerCommandLineArgs args = ServerCommandLineArgs.parse( argv );
        if ( args.version() )
        {
            System.out.println( "neo5j " + neo5jVersion() );
        }
        else
        {
            int status = new ArbiterBootstrapper().start( args.homeDir(), args.configFile() );
            if ( status != 0 )
            {
                System.exit( status );
            }
        }
    }

    public static void start( String[] args )
    {
        bootstrapper = new BlockingBootstrapper( new ArbiterBootstrapper() );
        System.exit( ServerBootstrapper.start( bootstrapper, args ) );
    }

    public static void stop( @SuppressWarnings( "UnusedParameters" ) String[] args )
    {
        if ( bootstrapper != null )
        {
            bootstrapper.stop();
        }
    }
}