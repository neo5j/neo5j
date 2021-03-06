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
package org.neo5j.cypher.internal.compiled_runtime.v3_2.codegen.profiling

import org.neo5j.cypher.internal.compatibility.v3_2.ProfileKernelStatisticProvider
import org.neo5j.cypher.internal.compiler.v3_2.planDescription.Id
import org.neo5j.cypher.internal.compiler.v3_2.spi.EmptyKernelStatisticProvider
import org.neo5j.cypher.internal.frontend.v3_2.test_helpers.CypherFunSuite
import org.neo5j.io.pagecache.tracing.cursor.DefaultPageCursorTracer

class ProfilingTracerTest extends CypherFunSuite {

  class Clock extends ProfilingTracer.Clock {
    var nanoTime: Long = 0L

    def progress(nanos: Long) {
      assert(nanos > 0, "time must move forwards")
      nanoTime += nanos
    }
  }

  test("shouldReportExecutionTimeOfQueryExecution") {
    // given
    val clock = new Clock
    val operatorId = new Id
    val tracer = new ProfilingTracer(clock, EmptyKernelStatisticProvider)
    val event = tracer.executeOperator(operatorId)

    // when
    clock.progress(516)
    event.close()

    // then
    tracer.timeOf(operatorId) should equal(516)
  }

  test("multiple uses of the same Id should aggregate spent time") {
    // given
    val clock = new Clock
    val operatorId = new Id
    val tracer = new ProfilingTracer(clock, EmptyKernelStatisticProvider)

    // when
    val event1 = tracer.executeOperator(operatorId)
    clock.progress(12)
    event1.close()

    val event2 = tracer.executeOperator(operatorId)
    clock.progress(45)
    event2.close()

    // then
    tracer.timeOf(operatorId) should equal(12 + 45)
  }

  test("shouldReportDbHitsOfQueryExecution") {
    // given
    val operatorId = new Id
    val tracer = new ProfilingTracer(EmptyKernelStatisticProvider)
    val event = tracer.executeOperator(operatorId)

    // when
    (0 until 516).foreach { _ =>
      event.dbHit()
    }

    event.close()

    // then
    tracer.dbHitsOf(operatorId) should equal(516)
  }

  test("shouldReportRowsOfQueryExecution") {
    // given
    val operatorId = new Id
    val tracer = new ProfilingTracer(EmptyKernelStatisticProvider)
    val event = tracer.executeOperator(operatorId)

    // when
    (0 until 516).foreach { _ =>
      event.row()
    }

    event.close()

    // then
    tracer.rowsOf(operatorId) should equal(516)

  }

  test("report page cache hits as part of profiling statistics") {
    val operatorId = new Id
    val cursorTracer = new DefaultPageCursorTracer
    var tracer = new ProfilingTracer(new ProfileKernelStatisticProvider(cursorTracer))
    val event = tracer.executeOperator(operatorId)

    1 to 100 foreach { _ => {
        val pin = cursorTracer.beginPin(false, 1, null)
        pin.hit()
        pin.done()
      }
    }

    event.close()

    val information = tracer.get(operatorId)
    information.pageCacheHits() should equal(100)
  }

  test("report page cache misses as part of profiling statistics") {
    val operatorId = new Id
    val cursorTracer = new DefaultPageCursorTracer
    var tracer = new ProfilingTracer(new ProfileKernelStatisticProvider(cursorTracer))
    val event = tracer.executeOperator(operatorId)

    1 to 17 foreach { _ => {
      val pin = cursorTracer.beginPin(false, 1, null)
      val pageFault = pin.beginPageFault()
      pageFault.done()
      pin.done()
    }
    }

    event.close()

    val information = tracer.get(operatorId)
    information.pageCacheMisses() should equal(17)
  }
}
