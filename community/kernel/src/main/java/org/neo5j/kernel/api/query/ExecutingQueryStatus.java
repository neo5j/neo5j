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
package org.neo5j.kernel.api.query;

import java.util.Map;

/**
 * Internal representation of the status of an executing query.
 * <p>
 * This is used for inspecting the state of a query.
 *
 * @see ExecutingQuery#status
 */
abstract class ExecutingQueryStatus
{
    static final String PLANNING_STATE = "planning", RUNNING_STATE = "running", WAITING_STATE = "waiting";
    /**
     * Time in nanoseconds that has been spent waiting in the current state.
     * This is the portion of wait time not included in the {@link ExecutingQuery#waitTimeNanos} field.
     *
     * @param currentTimeNanos
     *         the current timestamp on the nano clock.
     * @return the time between the time this state started waiting and the provided timestamp.
     */
    abstract long waitTimeNanos( long currentTimeNanos );

    abstract Map<String,Object> toMap( long currentTimeNanos );

    abstract String name();

    boolean isPlanning()
    {
        return false;
    }
}
