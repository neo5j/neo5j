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
package org.neo5j.harness.internal;

import java.io.File;
import java.util.Map;

import org.neo5j.kernel.configuration.Config;
import org.neo5j.kernel.impl.factory.GraphDatabaseFacadeFactory;
import org.neo5j.logging.FormattedLogProvider;
import org.neo5j.server.AbstractNeoServer;
import org.neo5j.server.CommunityNeoServer;

public class InProcessServerBuilder extends AbstractInProcessServerBuilder
{
    public InProcessServerBuilder()
    {
        this( new File( System.getProperty( "java.io.tmpdir" ) ) );
    }

    public InProcessServerBuilder( File workingDir )
    {
        super( workingDir );
    }

    @Override
    protected AbstractNeoServer createNeoServer( Map<String,String> config,
            GraphDatabaseFacadeFactory.Dependencies dependencies, FormattedLogProvider userLogProvider )
    {
        return new CommunityNeoServer( Config.embeddedDefaults( config ), dependencies, userLogProvider );
    }
}
