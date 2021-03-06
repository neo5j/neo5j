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
package org.neo5j.kernel.api.constraints;

import org.neo5j.kernel.api.TokenNameLookup;
import org.neo5j.kernel.api.exceptions.schema.CreateConstraintFailureException;
import org.neo5j.kernel.api.schema_new.SchemaDescriptor;
import org.neo5j.kernel.api.schema_new.constaints.ConstraintDescriptor;

/**
 * Interface describing a property constraint.
 *
 * @deprecated use {@link ConstraintDescriptor} instead.
 */
@Deprecated
public interface PropertyConstraint
{
    interface ChangeVisitor
    {
        void visitAddedUniquePropertyConstraint( UniquenessConstraint constraint );

        void visitRemovedUniquePropertyConstraint( UniquenessConstraint constraint );

        void visitAddedNodeKeyConstraint( NodeKeyConstraint constraint );

        void visitRemovedNodeKeyConstraint( NodeKeyConstraint constraint );

        void visitAddedNodePropertyExistenceConstraint( NodePropertyExistenceConstraint constraint )
                throws CreateConstraintFailureException;

        void visitRemovedNodePropertyExistenceConstraint( NodePropertyExistenceConstraint constraint );

        void visitAddedRelationshipPropertyExistenceConstraint( RelationshipPropertyExistenceConstraint constraint )
                throws CreateConstraintFailureException;

        void visitRemovedRelationshipPropertyExistenceConstraint( RelationshipPropertyExistenceConstraint constraint );
    }

    void added( ChangeVisitor visitor ) throws CreateConstraintFailureException;

    void removed( ChangeVisitor visitor );

    SchemaDescriptor descriptor();

    String userDescription( TokenNameLookup tokenNameLookup );

    @Override
    boolean equals( Object o );

    @Override
    int hashCode();

    @Override
    String toString();
}
