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
package org.neo5j.graphalgo.shortestpath;

import org.neo5j.graphalgo.CostEvaluator;
import org.neo5j.graphalgo.impl.shortestpath.SingleSourceShortestPath;
import org.neo5j.graphalgo.impl.shortestpath.SingleSourceShortestPathDijkstra;
import org.neo5j.graphdb.Direction;
import org.neo5j.graphdb.Node;
import org.neo5j.graphdb.Relationship;
import org.neo5j.graphdb.RelationshipType;

public class SingleSourceShortestPathDijkstraTest extends
    SingleSourceShortestPathTest
{
    protected SingleSourceShortestPath<Integer> getSingleSourceAlgorithm(
        Node startNode )
    {
        return new SingleSourceShortestPathDijkstra<Integer>( 0, startNode,
            new CostEvaluator<Integer>()
            {
                public Integer getCost( Relationship relationship,
                    Direction direction )
                {
                    return 1;
                }
            }, new org.neo5j.graphalgo.impl.util.IntegerAdder(),
            new org.neo5j.graphalgo.impl.util.IntegerComparator(),
            Direction.BOTH, MyRelTypes.R1 );
    }

    protected SingleSourceShortestPath<Integer> getSingleSourceAlgorithm(
        Node startNode, Direction direction, RelationshipType... relTypes )
    {
        return new SingleSourceShortestPathDijkstra<Integer>( 0, startNode,
            new CostEvaluator<Integer>()
            {
                public Integer getCost( Relationship relationship,
                    Direction direction )
                {
                    return 1;
                }
            }, new org.neo5j.graphalgo.impl.util.IntegerAdder(),
            new org.neo5j.graphalgo.impl.util.IntegerComparator(), direction,
            relTypes );
    }
}
