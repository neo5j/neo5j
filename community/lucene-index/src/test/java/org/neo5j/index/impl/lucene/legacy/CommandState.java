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
package org.neo5j.index.impl.lucene.legacy;

import org.neo5j.graphdb.GraphDatabaseService;
import org.neo5j.graphdb.Node;
import org.neo5j.graphdb.Transaction;
import org.neo5j.graphdb.index.Index;

public class CommandState
{
    final Index<Node> index;
    final GraphDatabaseService graphDb;
    public volatile Transaction tx;
    public volatile boolean alive = true;
    public volatile Node node;

    public CommandState( Index<Node> index, GraphDatabaseService graphDb, Node node )
    {
        this.index = index;
        this.graphDb = graphDb;
        this.node = node;
    }
}
