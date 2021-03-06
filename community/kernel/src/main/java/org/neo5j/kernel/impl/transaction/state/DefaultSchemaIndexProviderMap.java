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
package org.neo5j.kernel.impl.transaction.state;

import org.neo5j.kernel.api.index.SchemaIndexProvider;
import org.neo5j.kernel.impl.api.index.SchemaIndexProviderMap;

public class DefaultSchemaIndexProviderMap implements SchemaIndexProviderMap
{
    private final SchemaIndexProvider indexProvider;

    public DefaultSchemaIndexProviderMap( SchemaIndexProvider indexProvider )
    {
        this.indexProvider = indexProvider;
    }

    @Override
    public SchemaIndexProvider getDefaultProvider()
    {
        return indexProvider;
    }

    @Override
    public SchemaIndexProvider apply( SchemaIndexProvider.Descriptor descriptor )
    {
        if ( indexProvider.getProviderDescriptor().getKey().equals( descriptor.getKey() ) )
        {
            return indexProvider;
        }

        throw new IllegalArgumentException( "Tried to get index provider for an existing index with provider " +
                descriptor + " whereas the default and only supported provider in this session is " +
                indexProvider.getProviderDescriptor() );
    }
}
