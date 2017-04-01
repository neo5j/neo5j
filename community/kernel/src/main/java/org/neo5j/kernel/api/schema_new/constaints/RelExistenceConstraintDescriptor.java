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
package org.neo5j.kernel.api.schema_new.constaints;

import org.neo5j.kernel.api.TokenNameLookup;
import org.neo5j.kernel.api.schema_new.RelationTypeSchemaDescriptor;

public class RelExistenceConstraintDescriptor extends ConstraintDescriptor
{
    private final RelationTypeSchemaDescriptor schema;

    RelExistenceConstraintDescriptor( RelationTypeSchemaDescriptor schema )
    {
        super( Type.EXISTS );
        this.schema = schema;
    }

    @Override
    public RelationTypeSchemaDescriptor schema()
    {
        return schema;
    }

    @Override
    public String prettyPrint( TokenNameLookup tokenNameLookup )
    {
        String typeName = escapeLabelOrRelTyp( tokenNameLookup.relationshipTypeGetName( schema.getRelTypeId() ) );
        String relName = typeName.toLowerCase();
        String propertyName = tokenNameLookup.propertyKeyGetName( schema.getPropertyId() );

        return String.format( "CONSTRAINT ON ()-[ %s:%s ]-() ASSERT exists(%s.%s)",
                relName, typeName, relName, propertyName );
    }
}