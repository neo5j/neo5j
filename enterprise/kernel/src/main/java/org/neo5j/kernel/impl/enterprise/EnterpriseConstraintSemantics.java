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
package org.neo5j.kernel.impl.enterprise;

import java.util.Iterator;
import java.util.function.BiPredicate;

import org.neo5j.cursor.Cursor;
import org.neo5j.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo5j.kernel.api.exceptions.schema.CreateConstraintFailureException;
import org.neo5j.kernel.api.exceptions.schema.NodePropertyExistenceException;
import org.neo5j.kernel.api.exceptions.schema.RelationshipPropertyExistenceException;
import org.neo5j.kernel.api.schema_new.LabelSchemaDescriptor;
import org.neo5j.kernel.api.schema_new.RelationTypeSchemaDescriptor;
import org.neo5j.kernel.api.schema_new.constaints.ConstraintDescriptor;
import org.neo5j.kernel.api.schema_new.constaints.NodeKeyConstraintDescriptor;
import org.neo5j.kernel.impl.constraints.StandardConstraintSemantics;
import org.neo5j.kernel.impl.store.record.ConstraintRule;
import org.neo5j.storageengine.api.NodeItem;
import org.neo5j.storageengine.api.RelationshipItem;
import org.neo5j.storageengine.api.StoreReadLayer;
import org.neo5j.storageengine.api.txstate.ReadableTransactionState;
import org.neo5j.storageengine.api.txstate.TxStateVisitor;

import static org.neo5j.kernel.api.exceptions.schema.ConstraintValidationException.Phase.VERIFICATION;
import static org.neo5j.kernel.impl.enterprise.PropertyExistenceEnforcer.getOrCreatePropertyExistenceEnforcerFrom;

public class EnterpriseConstraintSemantics extends StandardConstraintSemantics
{
    @Override
    protected ConstraintDescriptor readNonStandardConstraint( ConstraintRule rule, String errorMessage )
    {
        if ( !rule.getConstraintDescriptor().enforcesPropertyExistence() )
        {
            throw new IllegalStateException( "Unsupported constraint type: " + rule );
        }
        return rule.getConstraintDescriptor();
    }

    @Override
    public ConstraintRule createNodeKeyConstraintRule(
            long ruleId, NodeKeyConstraintDescriptor descriptor, long indexId )
    {
        return ConstraintRule.constraintRule( ruleId, descriptor, indexId );
    }

    @Override
    public ConstraintRule createExistenceConstraint( long ruleId, ConstraintDescriptor descriptor )
    {
        return ConstraintRule.constraintRule( ruleId, descriptor );
    }

    @Override
    public void validateNodePropertyExistenceConstraint( Iterator<Cursor<NodeItem>> allNodes,
            LabelSchemaDescriptor descriptor, BiPredicate<NodeItem,Integer> hasPropertyCheck )
            throws CreateConstraintFailureException
    {
        while ( allNodes.hasNext() )
        {
            try ( Cursor<NodeItem> cursor = allNodes.next() )
            {
                NodeItem node = cursor.get();
                for ( int propertyKey : descriptor.getPropertyIds() )
                {
                    validateNodePropertyExistenceConstraint( node, propertyKey, descriptor, hasPropertyCheck );
                }
            }
        }
    }

    @Override
    public void validateNodeKeyConstraint( Iterator<Cursor<NodeItem>> allNodes,
            LabelSchemaDescriptor descriptor, BiPredicate<NodeItem,Integer> hasPropertyCheck )
            throws CreateConstraintFailureException
    {
        validateNodePropertyExistenceConstraint( allNodes, descriptor, hasPropertyCheck );
    }

    private void validateNodePropertyExistenceConstraint( NodeItem node, int propertyKey,
        LabelSchemaDescriptor descriptor, BiPredicate<NodeItem, Integer> hasPropertyCheck ) throws
            CreateConstraintFailureException
    {
        if ( !hasPropertyCheck.test( node, propertyKey ) )
        {
            throw createConstraintFailure(
                new NodePropertyExistenceException( descriptor, VERIFICATION, node.id() ) );
        }
    }

    @Override
    public void validateRelationshipPropertyExistenceConstraint( Cursor<RelationshipItem> allRelationships,
            RelationTypeSchemaDescriptor descriptor, BiPredicate<RelationshipItem,Integer> hasPropertyCheck )
            throws CreateConstraintFailureException
    {
        while ( allRelationships.next() )
        {
            RelationshipItem relationship = allRelationships.get();
            for ( int propertyId : descriptor.getPropertyIds() )
            {
                if ( relationship.type() == descriptor.getRelTypeId() &&
                        !hasPropertyCheck.test( relationship, propertyId ) )
                {
                    throw createConstraintFailure(
                            new RelationshipPropertyExistenceException( descriptor, VERIFICATION, relationship.id() ) );
                }
            }
        }
    }

    private CreateConstraintFailureException createConstraintFailure( ConstraintValidationException it )
    {
        return new CreateConstraintFailureException( it.constraint(), it );
    }

    @Override
    public TxStateVisitor decorateTxStateVisitor( StoreReadLayer storeLayer, ReadableTransactionState txState,
            TxStateVisitor visitor )
    {
        if ( !txState.hasDataChanges() )
        {
            // If there are no data changes, there is no need to enforce constraints. Since there is no need to
            // enforce constraints, there is no need to build up the state required to be able to enforce constraints.
            // In fact, it might even be counter productive to build up that state, since if there are no data changes
            // there would be schema changes instead, and in that case we would throw away the schema-dependant state
            // we just built when the schema changing transaction commits.
            return visitor;
        }
        return getOrCreatePropertyExistenceEnforcerFrom( storeLayer )
                .decorate( visitor, txState, storeLayer );
    }
}
