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
import org.neo5j.cypher.internal.frontend.v3_2.ParameterWrongTypeException
import org.neo5j.graphdb.Node

case class LabelsFunction(nodeExpr: Expression) extends NullInNullOutExpression(nodeExpr) {

  override def compute(value: Any, m: ExecutionContext)
                      (implicit state: QueryState): Any = value match {
    case n: Node =>
      val ctx = state.query
      ctx.getLabelsForNode(n.getId).map(ctx.getLabelName).toList
    case x => throw new ParameterWrongTypeException("Expected a Node, got: " + x)
  }

  override def rewrite(f: (Expression) => Expression) = f(LabelsFunction(nodeExpr.rewrite(f)))

  override def arguments = Seq(nodeExpr)

  override def symbolTableDependencies = nodeExpr.symbolTableDependencies
}
