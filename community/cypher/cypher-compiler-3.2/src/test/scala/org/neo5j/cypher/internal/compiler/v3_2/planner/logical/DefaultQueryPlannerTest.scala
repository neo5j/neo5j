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
package org.neo5j.cypher.internal.compiler.v3_2.planner.logical

import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import org.neo5j.cypher.internal.compiler.v3_2.planner._
import org.neo5j.cypher.internal.compiler.v3_2.planner.logical.Metrics.QueryGraphSolverInput
import org.neo5j.cypher.internal.compiler.v3_2.planner.logical.plans._
import org.neo5j.cypher.internal.compiler.v3_2.planner.logical.steps.LogicalPlanProducer
import org.neo5j.cypher.internal.compiler.v3_2.spi.PlanContext
import org.neo5j.cypher.internal.frontend.v3_2.ast.{ASTAnnotationMap, Expression, Hint}
import org.neo5j.cypher.internal.frontend.v3_2.phases.devNullLogger
import org.neo5j.cypher.internal.frontend.v3_2.symbols._
import org.neo5j.cypher.internal.frontend.v3_2.test_helpers.CypherFunSuite
import org.neo5j.cypher.internal.frontend.v3_2.{ExpressionTypeInfo, SemanticTable}
import org.neo5j.cypher.internal.ir.v3_2._

class DefaultQueryPlannerTest extends CypherFunSuite with LogicalPlanningTestSupport2 {

  test("adds ProduceResult with a single node") {
    val result = createProduceResultOperator(Seq("a"), SemanticTable().addNode(varFor("a")))

    result.columns should equal(Seq("a"))
  }

  test("adds ProduceResult with a single relationship") {
    val result = createProduceResultOperator(Seq("r"), SemanticTable().addRelationship(varFor("r")))

    result.columns should equal(Seq("r"))
  }

  test("adds ProduceResult with a single value") {
    val expr = varFor("x")
    val types = ASTAnnotationMap.empty[Expression, ExpressionTypeInfo].updated(expr, ExpressionTypeInfo(CTFloat, None))

    val result = createProduceResultOperator(Seq("x"), semanticTable = SemanticTable(types = types))

    result.columns should equal(Seq("x"))
  }

  private def createProduceResultOperator(columns: Seq[String], semanticTable: SemanticTable): ProduceResult = {
    implicit val planningContext = mockLogicalPlanningContext(semanticTable)

    val inputPlan = mock[LogicalPlan]
    when(inputPlan.availableSymbols).thenReturn(columns.map(IdName.apply).toSet)

    val queryPlanner = QueryPlanner(planSingleQuery = new FakePlanner(inputPlan))

    val pq = RegularPlannerQuery(horizon = RegularQueryProjection(columns.map(c => c -> varFor(c)).toMap))

    val union = UnionQuery(Seq(pq), distinct = false, columns.map(IdName.apply), periodicCommit = None)

    val (_, result) = queryPlanner.plan(union)

    result shouldBe a [ProduceResult]

    result.asInstanceOf[ProduceResult]
  }

  test("should set strictness when needed") {
    // given
    val plannerQuery = mock[RegularPlannerQuery with CardinalityEstimation]
    when(plannerQuery.preferredStrictness).thenReturn(Some(LazyMode))
    when(plannerQuery.queryGraph).thenReturn(QueryGraph.empty)
    when(plannerQuery.lastQueryGraph).thenReturn(QueryGraph.empty)
    when(plannerQuery.horizon).thenReturn(RegularQueryProjection())
    when(plannerQuery.lastQueryHorizon).thenReturn(RegularQueryProjection())
    when(plannerQuery.tail).thenReturn(None)
    when(plannerQuery.allHints).thenReturn(Set[Hint]())

    val lp = {
      val plan = SingleRow()(plannerQuery)
      Projection(plan, Map.empty)(plannerQuery)
    }

    val context = mock[LogicalPlanningContext]
    when(context.config).thenReturn(QueryPlannerConfiguration.default)
    when(context.input).thenReturn(QueryGraphSolverInput.empty)
    when(context.strategy).thenReturn(new QueryGraphSolver with PatternExpressionSolving {
      override def plan(queryGraph: QueryGraph)(implicit context: LogicalPlanningContext): LogicalPlan = lp
    })
    when(context.withStrictness(any())).thenReturn(context)
    val producer = mock[LogicalPlanProducer]
    when(producer.planStarProjection(any(), any(), any())(any())).thenReturn(lp)
    when(producer.planEmptyProjection(any())(any())).thenReturn(lp)
    when(context.logicalPlanProducer).thenReturn(producer)
    val queryPlanner = QueryPlanner(planSingleQuery = PlanSingleQuery())

    // when
    val query = UnionQuery(Seq(plannerQuery), distinct = false, Seq.empty, None)
    queryPlanner.plan(query)(context)

    // then
    verify(context, times(1)).withStrictness(LazyMode)
  }

  class FakePlanner(result: LogicalPlan) extends LogicalPlanningFunction1[PlannerQuery, LogicalPlan] {
    def apply(input: PlannerQuery)(implicit context: LogicalPlanningContext): LogicalPlan = result
  }

  private def mockLogicalPlanningContext(semanticTable: SemanticTable) = LogicalPlanningContext(
    planContext = mock[PlanContext],
    logicalPlanProducer = LogicalPlanProducer(mock[Metrics.CardinalityModel]),
    metrics = mock[Metrics],
    semanticTable = semanticTable,
    strategy = mock[QueryGraphSolver],
    config = QueryPlannerConfiguration.default,
    notificationLogger = devNullLogger)
}