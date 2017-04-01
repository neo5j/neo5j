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
package org.neo5j.kernel.impl.api.index;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import org.neo5j.kernel.api.index.IndexUpdater;
import org.neo5j.kernel.api.schema_new.index.NewIndexDescriptor;
import org.neo5j.kernel.api.schema_new.index.NewIndexDescriptorFactory;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IndexUpdaterMapTest
{
    private final long transactionId = 42L;

    private IndexMap indexMap;

    private IndexProxy indexProxy1;
    private NewIndexDescriptor indexDescriptor1;
    private IndexUpdater indexUpdater1;

    private IndexProxy indexProxy2;
    private NewIndexDescriptor indexDescriptor2;

    private IndexUpdaterMap updaterMap;

    @Before
    public void before() throws IOException
    {
        indexMap = new IndexMap();

        indexProxy1 = mock( IndexProxy.class );
        indexDescriptor1 = NewIndexDescriptorFactory.forLabel( 2, 3 );
        indexUpdater1 = mock( IndexUpdater.class );
        when( indexProxy1.getDescriptor() ).thenReturn( indexDescriptor1 );
        when( indexProxy1.newUpdater( any( IndexUpdateMode.class ) ) ).thenReturn( indexUpdater1 );

        indexProxy2 = mock( IndexProxy.class );
        indexDescriptor2 = NewIndexDescriptorFactory.forLabel( 5, 6 );
        IndexUpdater indexUpdater2 = mock( IndexUpdater.class );
        when( indexProxy2.getDescriptor() ).thenReturn( indexDescriptor2 );
        when( indexProxy2.newUpdater( any( IndexUpdateMode.class ) ) ).thenReturn( indexUpdater2 );

        updaterMap = new IndexUpdaterMap( indexMap, IndexUpdateMode.ONLINE );
    }

    @Test
    public void shouldRetrieveUpdaterFromIndexMapForExistingIndex() throws Exception
    {
        // given
        indexMap.putIndexProxy( 0, indexProxy1 );

        // when
        IndexUpdater updater = updaterMap.getUpdater( indexDescriptor1.schema() );

        // then
        assertEquals( indexUpdater1, updater );
        assertEquals( 1, updaterMap.size() );
    }

    @Test
    public void shouldRetrieveUpdateUsingLabelAndProperty()
    {
        // given
        indexMap.putIndexProxy( 0, indexProxy1 );

        // when
        IndexUpdater updater = updaterMap.getUpdater( indexDescriptor1.schema() );

        // then
        assertThat( updater, equalTo( indexUpdater1 ) );
    }

    @Test
    public void shouldRetrieveSameUpdaterFromIndexMapForExistingIndexWhenCalledTwice() throws Exception
    {
        // given
        indexMap.putIndexProxy( 0, indexProxy1 );

        // when
        IndexUpdater updater1 = updaterMap.getUpdater( indexDescriptor1.schema() );
        IndexUpdater updater2 = updaterMap.getUpdater( indexDescriptor1.schema() );

        // then
        assertEquals( updater1, updater2 );
        assertEquals( 1, updaterMap.size() );
    }

    @Test
    public void shouldRetrieveNoUpdaterForNonExistingIndex() throws Exception
    {
        // when
        IndexUpdater updater = updaterMap.getUpdater( indexDescriptor1.schema() );

        // then
        assertNull( updater );
        assertTrue( "updater map must be empty", updaterMap.isEmpty() );
    }

    @Test
    public void shouldCloseAllUpdaters() throws Exception
    {
        // given
        indexMap.putIndexProxy( 0, indexProxy1 );
        indexMap.putIndexProxy( 1, indexProxy2 );

        IndexUpdater updater1 = updaterMap.getUpdater( indexDescriptor1.schema() );
        IndexUpdater updater2 = updaterMap.getUpdater( indexDescriptor2.schema() );

        // hen
        updaterMap.close();

        // then
        verify( updater1 ).close();
        verify( updater2 ).close();

        assertTrue( "updater map must be empty", updaterMap.isEmpty() );
    }

}