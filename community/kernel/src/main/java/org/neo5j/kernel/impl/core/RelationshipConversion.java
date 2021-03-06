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

import java.util.NoSuchElementException;

import org.neo5j.graphdb.Relationship;
import org.neo5j.graphdb.ResourceIterator;
import org.neo5j.kernel.api.Statement;
import org.neo5j.kernel.impl.api.RelationshipVisitor;
import org.neo5j.kernel.impl.api.store.RelationshipIterator;

public class RelationshipConversion implements RelationshipVisitor<RuntimeException>, ResourceIterator<Relationship>
{
    private final NodeProxy.NodeActions actions;
    RelationshipIterator iterator;
    Statement statement;
    private Relationship next;

    public RelationshipConversion( NodeProxy.NodeActions actions )
    {
        this.actions = actions;
    }

    @Override
    public void visit( long relId, int type, long startNode, long endNode ) throws RuntimeException
    {
        next = actions.newRelationshipProxy( relId, startNode, type, endNode );
    }

    @Override
    public boolean hasNext()
    {
        return iterator.hasNext();
    }

    @Override
    public Relationship next()
    {
        if ( !hasNext() )
        {
            throw new NoSuchElementException();
        }
        iterator.relationshipVisit( iterator.next(), this );
        Relationship current = next;
        next = null;
        return current;
    }

    @Override
    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close()
    {
        statement.close();
    }
}
