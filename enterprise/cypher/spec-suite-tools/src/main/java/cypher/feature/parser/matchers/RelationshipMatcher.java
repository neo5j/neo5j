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
package cypher.feature.parser.matchers;

import org.neo5j.graphdb.Relationship;

public class RelationshipMatcher implements ValueMatcher
{
    private final String relationshipTypeName;
    private final MapMatcher propertyMatcher;

    public RelationshipMatcher( String relationshipTypeName, MapMatcher propertyMatcher )
    {
        this.relationshipTypeName = relationshipTypeName;
        this.propertyMatcher = propertyMatcher;
    }

    @Override
    public boolean matches( Object value )
    {
        if ( value instanceof Relationship )
        {
            Relationship relationship = (Relationship) value;

            return relationship.getType().name().equals( relationshipTypeName )
                   && propertyMatcher.matches( relationship.getAllProperties() );
        }
        return false;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "[:" ).append( relationshipTypeName );
        sb.append( " " ).append( propertyMatcher ).append( "]" );
        return sb.toString();
    }
}
