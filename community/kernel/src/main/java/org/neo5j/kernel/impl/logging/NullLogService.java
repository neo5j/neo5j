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
package org.neo5j.kernel.impl.logging;

import org.neo5j.logging.Log;
import org.neo5j.logging.LogProvider;
import org.neo5j.logging.NullLog;
import org.neo5j.logging.NullLogProvider;

public class NullLogService implements LogService
{
    private static final NullLogService INSTANCE = new NullLogService();

    public final NullLogProvider nullLogProvider = NullLogProvider.getInstance();
    public final NullLog nullLog = NullLog.getInstance();

    private NullLogService()
    {
    }

    public static NullLogService getInstance()
    {
        return INSTANCE;
    }

    @Override
    public LogProvider getUserLogProvider()
    {
        return nullLogProvider;
    }

    @Override
    public Log getUserLog( Class loggingClass )
    {
        return nullLog;
    }

    @Override
    public LogProvider getInternalLogProvider()
    {
        return nullLogProvider;
    }

    @Override
    public Log getInternalLog( Class loggingClass )
    {
        return nullLog;
    }
}
