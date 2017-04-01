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
package org.neo5j.server.security.enterprise.auth.plugin;

import java.util.Arrays;
import java.util.Collections;

import org.neo5j.server.security.enterprise.auth.plugin.api.AuthToken;
import org.neo5j.server.security.enterprise.auth.plugin.api.PredefinedRoles;
import org.neo5j.server.security.enterprise.auth.plugin.spi.AuthInfo;
import org.neo5j.server.security.enterprise.auth.plugin.spi.AuthPlugin;

public class TestAuthPlugin extends AuthPlugin.Adapter
{
    @Override
    public String name()
    {
        return getClass().getSimpleName();
    }

    @Override
    public AuthInfo authenticateAndAuthorize( AuthToken authToken )
    {
        String principal = authToken.principal();
        char[] credentials = authToken.credentials();

        if ( principal.equals( "neo5j" ) && Arrays.equals( credentials, "neo5j".toCharArray() ) )
        {
            return AuthInfo.of( "neo5j", Collections.singleton( PredefinedRoles.READER ) );
        }
        return null;
    }
}