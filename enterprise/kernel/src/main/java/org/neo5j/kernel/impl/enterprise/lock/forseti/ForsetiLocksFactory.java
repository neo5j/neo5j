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
package org.neo5j.kernel.impl.enterprise.lock.forseti;

import java.time.Clock;

import org.neo5j.helpers.Service;
import org.neo5j.kernel.configuration.Config;
import org.neo5j.kernel.impl.locking.Locks;
import org.neo5j.kernel.impl.locking.ResourceTypes;
import org.neo5j.storageengine.api.lock.ResourceType;

@Service.Implementation( Locks.Factory.class )
public class ForsetiLocksFactory extends Locks.Factory
{
    public ForsetiLocksFactory()
    {
        super( "forseti" );
    }

    @Override
    public Locks newInstance( Config config, Clock clock, ResourceType[] resourceTypes )
    {
        return new ForsetiLockManager( config, clock, ResourceTypes.values() );
    }
}
