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
package org.neo5j.cypher.internal

import org.neo5j.cypher.internal.compatibility.{v2_3, v3_1, _}
import org.neo5j.cypher.internal.compiler.v3_2._
import org.neo5j.cypher.internal.spi.v3_2.codegen.GeneratedQueryStructure
import org.neo5j.cypher.{CypherPlanner, CypherRuntime}
import org.neo5j.kernel.GraphDatabaseQueryService
import org.neo5j.kernel.api.KernelAPI
import org.neo5j.kernel.monitoring.{Monitors => KernelMonitors}
import org.neo5j.logging.LogProvider

class EnterpriseCompatibilityFactory(inner: CompatibilityFactory, graph: GraphDatabaseQueryService,
                                     kernelAPI: KernelAPI, kernelMonitors: KernelMonitors,
                                     logProvider: LogProvider) extends CompatibilityFactory {
  override def create(spec: PlannerSpec_v2_3, config: CypherCompilerConfiguration): v2_3.Compatibility =
    inner.create(spec, config)

  override def create(spec: PlannerSpec_v3_1, config: CypherCompilerConfiguration): v3_1.Compatibility =
    inner.create(spec, config)

  override def create(spec: PlannerSpec_v3_2, config: CypherCompilerConfiguration): v3_2.Compatibility[_] =
    (spec.planner, spec.runtime) match {
      case (CypherPlanner.rule, _) => inner.create(spec, config)

      case (_, CypherRuntime.compiled) | (_, CypherRuntime.default) =>
        val contextCreator = new EnterpriseContextCreator(GeneratedQueryStructure)
        v3_2.CostCompatibility(config, CompilerEngineDelegator.CLOCK, kernelMonitors, kernelAPI, logProvider.getLog
        (getClass), spec.planner, spec.runtime, spec.updateStrategy, EnterpriseRuntimeBuilder, contextCreator)

      case _ => inner.create(spec, config)
    }
}
