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

import org.neo5j.cypher.internal.compiler.v3_2.commands.values.UnresolvedProperty
import org.neo5j.cypher.internal.frontend.v3_2.test_helpers.CypherFunSuite

class LiteralMapTest extends CypherFunSuite {

  test("should_present_all_child_expressions") {
    val x = Variable("x")
    // given
    val propX = Property(x, UnresolvedProperty("foo"))
    val count = CountStar()
    val literalMap = LiteralMap(Map("x" -> propX, "count" -> count))

    // when
    val subExpressions = literalMap.arguments.toSet

    // then
    subExpressions should equal(Set(propX, count))
  }
}