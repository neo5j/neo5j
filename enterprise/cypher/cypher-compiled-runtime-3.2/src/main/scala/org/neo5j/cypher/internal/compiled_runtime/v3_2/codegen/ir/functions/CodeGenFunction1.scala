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
package org.neo5j.cypher.internal.compiled_runtime.v3_2.codegen.ir.functions

import org.neo5j.cypher.internal.compiled_runtime.v3_2.codegen.CodeGenContext
import org.neo5j.cypher.internal.compiled_runtime.v3_2.codegen.ir.expressions._
import org.neo5j.cypher.internal.compiled_runtime.v3_2.codegen.spi.MethodStructure
import org.neo5j.cypher.internal.frontend.v3_2.InternalException

sealed trait CodeGenFunction1 extends ((CodeGenExpression) => CodeGenExpression)

case object IdCodeGenFunction extends CodeGenFunction1 {

  override def apply(arg: CodeGenExpression): CodeGenExpression = arg match {
    case n: NodeExpression => load(n.nodeIdVar.name)
    case n: NodeProjection => load(n.nodeIdVar.name)
    case r: RelationshipExpression => load(r.relId.name)
    case r: RelationshipProjection => load(r.relId.name)
    case e => throw new InternalException(s"id function only accepts nodes or relationships not $e")
  }

  private def load(variable: String) = new CodeGenExpression {
    override def generateExpression[E](structure: MethodStructure[E])
                                      (implicit
                                       context: CodeGenContext): E =
      structure.loadVariable(variable)

    override def init[E](generator: MethodStructure[E])(implicit context: CodeGenContext) = {}

    override def nullable(implicit context: CodeGenContext): Boolean = false

    override def codeGenType(implicit context: CodeGenContext): CypherCodeGenType = CodeGenType.primitiveInt
  }
}

case object TypeCodeGenFunction extends CodeGenFunction1 {

  override def apply(arg: CodeGenExpression): CodeGenExpression = arg match {
    case r: RelationshipExpression => TypeOf(r.relId)
    case r: RelationshipProjection => TypeOf(r.relId)
    case e => throw new InternalException(s"type function only accepts relationships $e")
  }
}
