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
package org.neo5j.kernel.impl.core;

import java.util.function.Supplier;

import org.neo5j.kernel.api.KernelAPI;
import org.neo5j.kernel.api.Statement;
import org.neo5j.kernel.api.exceptions.schema.IllegalTokenNameException;
import org.neo5j.kernel.impl.store.id.IdGeneratorFactory;
import org.neo5j.kernel.impl.store.id.IdType;

public class DefaultPropertyTokenCreator extends IsolatedTransactionTokenCreator
{
    public DefaultPropertyTokenCreator( Supplier<KernelAPI> kernelSupplier, IdGeneratorFactory idGeneratorFactory )
    {
        super( kernelSupplier, idGeneratorFactory );
    }

    @Override
    protected int createKey( Statement statement, String name ) throws IllegalTokenNameException
    {
        int id = (int) idGeneratorFactory.get( IdType.PROPERTY_KEY_TOKEN ).nextId();
        statement.tokenWriteOperations().propertyKeyCreateForName( name, id );
        return id;
    }
}