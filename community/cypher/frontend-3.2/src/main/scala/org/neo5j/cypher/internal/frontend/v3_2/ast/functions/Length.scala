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
package org.neo5j.cypher.internal.frontend.v3_2.ast.functions

import org.neo5j.cypher.internal.frontend.v3_2.ast.Expression.SemanticContext
import org.neo5j.cypher.internal.frontend.v3_2.ast.{ExpressionSignature, Function, FunctionInvocation, SimpleTypedFunction}
import org.neo5j.cypher.internal.frontend.v3_2.notification.LengthOnNonPathNotification
import org.neo5j.cypher.internal.frontend.v3_2.symbols._
import org.neo5j.cypher.internal.frontend.v3_2.{SemanticCheckResult, SemanticState}

case object Length extends Function with SimpleTypedFunction {
  def name = "length"

  //NOTE using CTString and CTCollection here is deprecated
  override val signatures = Vector(
    ExpressionSignature(Vector(CTString), CTInteger),
    ExpressionSignature(Vector(CTList(CTAny)), CTInteger),
    ExpressionSignature(Vector(CTPath), CTInteger)
  )

  override def semanticCheck(ctx: SemanticContext, invocation: FunctionInvocation) =
    super.semanticCheck(ctx, invocation) chain checkForInvalidUsage(ctx, invocation)

  def checkForInvalidUsage(ctx: SemanticContext, invocation: FunctionInvocation) = (originalState: SemanticState) => {
    val newState = invocation.args.foldLeft(originalState) {
      case (state, expr) if state.expressionType(expr).actual != CTPath.invariant =>
        state.addNotification(LengthOnNonPathNotification(expr.position))
      case (state, expr) =>
        state
    }

    SemanticCheckResult(newState, Seq.empty)
  }
}
