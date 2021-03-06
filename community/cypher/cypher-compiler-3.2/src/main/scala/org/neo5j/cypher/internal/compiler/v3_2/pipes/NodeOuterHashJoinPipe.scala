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
package org.neo5j.cypher.internal.compiler.v3_2.pipes

import org.neo5j.cypher.internal.compiler.v3_2.ExecutionContext
import org.neo5j.cypher.internal.compiler.v3_2.planDescription.Id
import org.neo5j.graphdb.Node

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

case class NodeOuterHashJoinPipe(nodeVariables: Set[String], source: Pipe, inner: Pipe, nullableVariables: Set[String])
                                (val id: Id = new Id)(implicit pipeMonitor: PipeMonitor)
  extends PipeWithSource(source, pipeMonitor) {
  val nullColumns: Map[String, Any] = nullableVariables.map(_ -> null).toMap

  protected def internalCreateResults(input: Iterator[ExecutionContext], state: QueryState): Iterator[ExecutionContext] = {

    if(input.isEmpty)
      return Iterator.empty

    val probeTable = buildProbeTableAndFindNullRows(input)

    val seenKeys = mutable.Set[IndexedSeq[Long]]()
    val joinedRows = (
      for {context <- inner.createResults(state)
           joinKey <- computeKey(context)}
      yield {
        val seq = probeTable(joinKey)
        seenKeys.add(joinKey)
        seq.map(context ++ _)
      }).flatten

    def rowsWithoutRhsMatch: Iterator[ExecutionContext] = (probeTable.keySet -- seenKeys).iterator.flatMap {
      x => probeTable(x).map(addNulls)
    }

    val rowsWithNullAsJoinKey = probeTable.nullRows.map(addNulls)

    rowsWithNullAsJoinKey ++ joinedRows ++ rowsWithoutRhsMatch
  }

  private def addNulls(in:ExecutionContext): ExecutionContext = in.newWith(nullColumns)

  private def buildProbeTableAndFindNullRows(input: Iterator[ExecutionContext]): ProbeTable = {
    val probeTable = new ProbeTable()

    for (context <- input) {
      val key = computeKey(context)

      key match {
        case Some(joinKey) => probeTable.addValue(joinKey, context)
        case None          => probeTable.addNull(context)
      }
    }

    probeTable
  }

  private val myVariables = nodeVariables.toIndexedSeq

  private def computeKey(context: ExecutionContext): Option[IndexedSeq[Long]] = {
    val key = new Array[Long](myVariables.length)

    for (idx <- 0 until myVariables.length) {
      key(idx) = context(myVariables(idx)) match {
        case n: Node => n.getId
        case _ => return None
      }
    }
    Some(key.toIndexedSeq)
  }
}

class ProbeTable() {
  private val table: mutable.HashMap[IndexedSeq[Long], mutable.MutableList[ExecutionContext]] =
    new mutable.HashMap[IndexedSeq[Long], mutable.MutableList[ExecutionContext]]

  private val rowsWithNullInKey: ListBuffer[ExecutionContext] = new ListBuffer[ExecutionContext]()

  def addValue(key: IndexedSeq[Long], newValue: ExecutionContext) {
    val values = table.getOrElseUpdate(key, mutable.MutableList.empty)
    values += newValue
  }

  def addNull(context: ExecutionContext) = rowsWithNullInKey += context

  val EMPTY = mutable.MutableList.empty
  def apply(key: IndexedSeq[Long]) = table.getOrElse(key, EMPTY)

  def keySet: collection.Set[IndexedSeq[Long]] = table.keySet

  def nullRows = rowsWithNullInKey.iterator
}
