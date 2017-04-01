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
package org.neo5j.kernel.impl.coreapi.schema;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

import org.neo5j.graphdb.GraphDatabaseService;
import org.neo5j.graphdb.Label;
import org.neo5j.graphdb.Node;
import org.neo5j.graphdb.Transaction;
import org.neo5j.graphdb.index.IndexPopulationProgress;
import org.neo5j.graphdb.schema.IndexDefinition;
import org.neo5j.graphdb.schema.Schema;
import org.neo5j.helpers.collection.Iterables;
import org.neo5j.test.TestGraphDatabaseFactory;
import org.neo5j.test.rule.fs.EphemeralFileSystemRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SchemaImplTest
{
    @Rule
    public EphemeralFileSystemRule fs = new EphemeralFileSystemRule();
    private GraphDatabaseService db;

    @Before
    public void createDb() throws Exception
    {
        db = new TestGraphDatabaseFactory().setFileSystem( fs.get() ).newImpermanentDatabase( new File( "mydb" ) );
    }

    @After
    public void shutdownDb() throws Exception
    {
        db.shutdown();
    }

    @Test
    public void testGetIndexPopulationProgress() throws Exception
    {
        assertFalse( indexExists( Label.label( "User" ) ) );

        // Create some nodes
        try ( Transaction tx = db.beginTx() )
        {
            Label label = Label.label( "User" );

            // Create a huge bunch of users so the index takes a while to build
            for ( int id = 0; id < 100000; id++ )
            {
                Node userNode = db.createNode( label );
                userNode.setProperty( "username", "user" + id + "@neo5j.org" );
            }
            tx.success();
        }

        // Create an index
        IndexDefinition indexDefinition;
        try ( Transaction tx = db.beginTx() )
        {
            Schema schema = db.schema();
            indexDefinition = schema.indexFor( Label.label( "User" ) )
                    .on( "username" )
                    .create();
            tx.success();
        }

        // Get state and progress
        try ( Transaction tx = db.beginTx() )
        {
            Schema schema = db.schema();
            Schema.IndexState state;

            IndexPopulationProgress progress;
            do
            {
                state = schema.getIndexState( indexDefinition );
                progress = schema.getIndexPopulationProgress( indexDefinition );

                assertTrue( progress.getCompletedPercentage() >= 0 );
                assertTrue( progress.getCompletedPercentage() <= 100 );
                Thread.sleep( 10 );
            }
            while ( state == Schema.IndexState.POPULATING );

            assertTrue( state == Schema.IndexState.ONLINE );
            assertEquals( 100.0f, progress.getCompletedPercentage(), 0.0f );
        }
    }

    private boolean indexExists( Label label )
    {
        try ( Transaction transaction = db.beginTx() )
        {
            Iterable<IndexDefinition> indexes = db.schema().getIndexes( label );
            IndexDefinition index = Iterables.firstOrNull( indexes );
            boolean exists = index != null;
            transaction.success();
            return exists;
        }
    }
}