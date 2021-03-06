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
package org.neo5j.kernel.impl.constraints;

import java.util.Iterator;
import java.util.function.BiPredicate;

import org.neo5j.cursor.Cursor;
import org.neo5j.kernel.api.exceptions.schema.CreateConstraintFailureException;
import org.neo5j.kernel.api.schema_new.LabelSchemaDescriptor;
import org.neo5j.kernel.api.schema_new.RelationTypeSchemaDescriptor;
import org.neo5j.kernel.api.schema_new.constaints.ConstraintDescriptor;
import org.neo5j.kernel.api.schema_new.constaints.NodeKeyConstraintDescriptor;
import org.neo5j.kernel.api.schema_new.constaints.UniquenessConstraintDescriptor;
import org.neo5j.kernel.impl.store.record.ConstraintRule;
import org.neo5j.storageengine.api.NodeItem;
import org.neo5j.storageengine.api.RelationshipItem;
import org.neo5j.storageengine.api.StoreReadLayer;
import org.neo5j.storageengine.api.txstate.ReadableTransactionState;
import org.neo5j.storageengine.api.txstate.TxStateVisitor;

/**
 * Implements semantics of constraint creation and enforcement.
 */
public interface ConstraintSemantics
{
    void validateNodeKeyConstraint( Iterator<Cursor<NodeItem>> allNodes, LabelSchemaDescriptor descriptor,
            BiPredicate<NodeItem,Integer> hasProperty ) throws CreateConstraintFailureException;

    void validateNodePropertyExistenceConstraint( Iterator<Cursor<NodeItem>> allNodes, LabelSchemaDescriptor descriptor,
            BiPredicate<NodeItem,Integer> hasProperty ) throws CreateConstraintFailureException;

    void validateRelationshipPropertyExistenceConstraint( Cursor<RelationshipItem> allRelationships,
            RelationTypeSchemaDescriptor descriptor, BiPredicate<RelationshipItem,Integer> hasPropertyCheck )
            throws CreateConstraintFailureException;

    ConstraintDescriptor readConstraint( ConstraintRule rule );

    ConstraintRule createUniquenessConstraintRule( long ruleId, UniquenessConstraintDescriptor descriptor, long indexId );

    ConstraintRule createNodeKeyConstraintRule( long ruleId, NodeKeyConstraintDescriptor descriptor, long indexId )
            throws CreateConstraintFailureException;

    ConstraintRule createExistenceConstraint( long ruleId, ConstraintDescriptor descriptor )
            throws CreateConstraintFailureException;

    TxStateVisitor decorateTxStateVisitor( StoreReadLayer storeLayer, ReadableTransactionState state,
            TxStateVisitor visitor );
}
