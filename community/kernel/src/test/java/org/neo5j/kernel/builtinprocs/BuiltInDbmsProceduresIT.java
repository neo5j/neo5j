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
package org.neo5j.kernel.builtinprocs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import org.neo5j.collection.RawIterator;
import org.neo5j.graphdb.factory.GraphDatabaseSettings;
import org.neo5j.kernel.api.exceptions.ProcedureException;
import org.neo5j.kernel.api.security.AnonymousContext;
import org.neo5j.kernel.impl.api.integrationtest.KernelIntegrationTest;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.neo5j.helpers.collection.Iterators.asList;
import static org.neo5j.kernel.api.proc.ProcedureSignature.procedureName;

public class BuiltInDbmsProceduresIT extends KernelIntegrationTest
{
    @Test
    public void listConfig() throws Exception
    {
        // When
        RawIterator<Object[],ProcedureException> stream =
                dbmsOperations().procedureCallDbms( procedureName( "dbms", "listConfig" ),
                        Arrays.asList( "" ).toArray(),
                        AnonymousContext.none() );

        // Then
        List<Object[]> config = asList( stream );
        List<String> names = config.stream()
                .map( o -> o[0].toString() )
                .collect( Collectors.toList() );

        // The size of the config is not fixed so just make sure it's the right magnitude
        assertTrue( names.size() > 10 );

        assertThat( names, hasItem( GraphDatabaseSettings.record_format.name() ) );

        // Should not contain "unsupported.*" configs
        assertEquals( names.stream()
                .filter( n -> n.startsWith( "unsupported" ) )
                .count(), 0 );
    }

    @Test
    public void listConfigWithASpecificConfigName() throws Exception
    {
        // When
        RawIterator<Object[],ProcedureException> stream =
                dbmsOperations().procedureCallDbms( procedureName( "dbms", "listConfig" ),
                        Arrays.asList( GraphDatabaseSettings.strict_config_validation.name() ).toArray(),
                        AnonymousContext.none() );

        // Then
        List<Object[]> config = asList( stream );

        assertEquals( 1, config.size() );
        assertArrayEquals( new Object[]{ "dbms.config.strict_validation",
                "A strict configuration validation will prevent the database from starting up if unknown " +
                        "configuration options are specified in the neo5j settings namespace (such as dbms., ha., " +
                        "cypher., etc). This is currently false by default but will be true by default in 4.0.",
                "false" }, config.get( 0 ) );
    }
}