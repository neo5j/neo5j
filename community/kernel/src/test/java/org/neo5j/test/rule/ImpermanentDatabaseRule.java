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
package org.neo5j.test.rule;

import org.neo5j.graphdb.factory.GraphDatabaseBuilder;
import org.neo5j.graphdb.factory.GraphDatabaseFactory;
import org.neo5j.logging.LogProvider;
import org.neo5j.test.TestGraphDatabaseFactory;

/**
 * JUnit @Rule for configuring, creating and managing an ImpermanentGraphDatabase instance.
 */
public class ImpermanentDatabaseRule extends DatabaseRule
{
    private final LogProvider userLogProvider;
    private final LogProvider internalLogProvider;

    public ImpermanentDatabaseRule()
    {
        this( null );
    }

    public ImpermanentDatabaseRule( LogProvider logProvider )
    {
        this.userLogProvider = logProvider;
        this.internalLogProvider = logProvider;
    }

    @Override
    public ImpermanentDatabaseRule startLazily()
    {
        return (ImpermanentDatabaseRule) super.startLazily();
    }

    @Override
    protected GraphDatabaseFactory newFactory()
    {
        return maybeSetInternalLogProvider( maybeSetUserLogProvider( new TestGraphDatabaseFactory() ) );
    }

    @Override
    protected GraphDatabaseBuilder newBuilder( GraphDatabaseFactory factory )
    {
        return ((TestGraphDatabaseFactory) factory).newImpermanentDatabaseBuilder();
    }

    protected final TestGraphDatabaseFactory maybeSetUserLogProvider( TestGraphDatabaseFactory factory )
    {
        return ( userLogProvider == null ) ? factory : factory.setUserLogProvider( userLogProvider );
    }

    protected final TestGraphDatabaseFactory maybeSetInternalLogProvider( TestGraphDatabaseFactory factory )
    {
        return ( internalLogProvider == null ) ? factory : factory.setInternalLogProvider( internalLogProvider );
    }
}
