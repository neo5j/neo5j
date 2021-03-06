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
package org.neo5j.cypher.internal.compiler.v3_2.planner.logical.cardinality

import org.neo5j.cypher.internal.frontend.v3_2.ast.Expression
import org.neo5j.cypher.internal.compiler.v3_2.spi.GraphStatistics
import org.neo5j.cypher.internal.frontend.v3_2.SemanticTable
import org.neo5j.cypher.internal.ir.v3_2.{Selections, Selectivity}

trait SelectivityEstimator extends (Expression => Selectivity) {
  self: SelectivityEstimator =>

  def combiner: SelectivityCombiner

  def and(predicates: Set[Expression], defaultSelectivity: Selectivity = Selectivity.ONE): Selectivity =
    combiner.andTogetherSelectivities(predicates.map(self).toIndexedSeq).getOrElse(defaultSelectivity)
}

case class DelegatingSelectivityEstimator(inner: SelectivityEstimator) extends SelectivityEstimator {
  def apply(expr: Expression) = inner(expr)
  def combiner = inner.combiner
}

// TODO: Fuse with ExpressionSelectivityCalculator
case class ExpressionSelectivityEstimator(selections: Selections,
                                          stats: GraphStatistics,
                                          semanticTable: SemanticTable,
                                          combiner: SelectivityCombiner)
  extends SelectivityEstimator {

  self =>

  private val calculator = ExpressionSelectivityCalculator(stats, combiner)

  def apply(expr: Expression) = calculator(expr)(semanticTable, selections)
}
