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
package org.neo5j.cypher.internal.compiler.v3_2.commands.expressions

import org.neo5j.cypher.internal.compiler.v3_2._
import org.neo5j.cypher.internal.compiler.v3_2.pipes.QueryState
import org.neo5j.cypher.internal.compiler.v3_2.pipes.aggregation._
import org.neo5j.cypher.internal.frontend.v3_2.SyntaxException
import org.neo5j.cypher.internal.frontend.v3_2.symbols.CypherType

abstract class AggregationExpression extends Expression {
  def apply(ctx: ExecutionContext)(implicit state: QueryState) =
    throw new UnsupportedOperationException("Aggregations should not be used like this.")

  def createAggregationFunction: AggregationFunction
}

abstract class AggregationWithInnerExpression(inner:Expression) extends AggregationExpression {
  if(inner.containsAggregate)
    throw new SyntaxException("Can't use aggregate functions inside of aggregate functions.")

  if(! inner.isDeterministic)
    throw new SyntaxException("Can't use non-deterministic (random) functions inside of aggregate functions.")

  def expectedInnerType: CypherType

  def arguments = Seq(inner)

  def symbolTableDependencies = inner.symbolTableDependencies
}
