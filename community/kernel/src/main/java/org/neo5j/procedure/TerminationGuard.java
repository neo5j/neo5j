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
package org.neo5j.procedure;

/**
 * TerminationGuard allows a long running procedure to check at regular intervals if the surrounding executing
 * query has been terminated by the user or a database administrator or was timed out for some other reason.
 *
 */
public interface TerminationGuard
{
    /**
     * Check that the surrounding executing query has not yet been terminated or timed out. Throws an appropriate
     * exception if it has.
     */
    void check();
}