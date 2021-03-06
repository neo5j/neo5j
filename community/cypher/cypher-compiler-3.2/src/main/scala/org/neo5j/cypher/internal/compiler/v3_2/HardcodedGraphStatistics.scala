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
package org.neo5j.cypher.internal.compiler.v3_2

import org.neo5j.cypher.internal.compiler.v3_2.spi.GraphStatistics
import org.neo5j.cypher.internal.frontend.v3_2.{LabelId, PropertyKeyId, RelTypeId}
import org.neo5j.cypher.internal.ir.v3_2.{Cardinality, Selectivity}


case object HardcodedGraphStatistics extends HardcodedGraphStatisticsValues

class HardcodedGraphStatisticsValues extends GraphStatistics {
  val NODES_CARDINALITY = Cardinality(10000)
  val NODES_WITH_LABEL_SELECTIVITY = Selectivity.of(0.2).get
  val NODES_WITH_LABEL_CARDINALITY = NODES_CARDINALITY * NODES_WITH_LABEL_SELECTIVITY
  val RELATIONSHIPS_CARDINALITY = Cardinality(50000)
  val INDEX_SELECTIVITY = Selectivity.of(.02).get
  val INDEX_PROPERTY_EXISTS_SELECTIVITY = Selectivity.of(.5).get

  def indexSelectivity(index: IndexDescriptor): Option[Selectivity] =
    Some(INDEX_SELECTIVITY * Selectivity.of(index.properties.length).get)

  def indexPropertyExistsSelectivity(index: IndexDescriptor): Option[Selectivity] =
    Some(INDEX_PROPERTY_EXISTS_SELECTIVITY * Selectivity.of(index.properties.length).get)

  def nodesWithLabelCardinality(labelId: Option[LabelId]): Cardinality =
    labelId.map(_ => NODES_WITH_LABEL_CARDINALITY).getOrElse(NODES_CARDINALITY)

  def cardinalityByLabelsAndRelationshipType(fromLabel: Option[LabelId], relTypeId: Option[RelTypeId], toLabel: Option[LabelId]): Cardinality =
    RELATIONSHIPS_CARDINALITY
}
