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
package org.neo5j.kernel.api.exceptions.index;

import org.junit.Test;

import org.neo5j.kernel.api.StatementConstants;
import org.neo5j.kernel.api.schema_new.LabelSchemaDescriptor;
import org.neo5j.kernel.api.schema_new.OrderedPropertyValues;
import org.neo5j.kernel.api.schema_new.SchemaDescriptorFactory;
import org.neo5j.kernel.api.schema_new.SchemaUtil;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class IndexEntryConflictExceptionTest
{
    public static final int labelId = 1;

    @Test
    public void shouldMakeEntryConflicts()
    {
        LabelSchemaDescriptor schema = SchemaDescriptorFactory.forLabel( labelId, 2 );
        IndexEntryConflictException e = new IndexEntryConflictException( 0L, 1L, "hi" );

        assertThat( e.evidenceMessage( SchemaUtil.idTokenNameLookup, schema ),
                equalTo( "Both Node(0) and Node(1) have the label `label[1]` and property `property[2]` = 'hi'" ) );
    }

    @Test
    public void shouldMakeEntryConflictsForOneNode()
    {
        LabelSchemaDescriptor schema = SchemaDescriptorFactory.forLabel( labelId, 2 );
        IndexEntryConflictException e = new IndexEntryConflictException( 0L, StatementConstants.NO_SUCH_NODE, "hi" );

        assertThat( e.evidenceMessage( SchemaUtil.idTokenNameLookup, schema ),
                equalTo( "Node(0) already exists with label `label[1]` and property `property[2]` = 'hi'" ) );
    }

    @Test
    public void shouldMakeCompositeEntryConflicts()
    {
        LabelSchemaDescriptor schema = SchemaDescriptorFactory.forLabel( labelId, 2, 3, 4 );
        OrderedPropertyValues values = OrderedPropertyValues.ofUndefined( true, "hi", new long[]{6L, 4L} );
        IndexEntryConflictException e = new IndexEntryConflictException( 0L, 1L, values );

        assertThat( e.evidenceMessage( SchemaUtil.idTokenNameLookup, schema ),
                equalTo( "Both Node(0) and Node(1) have the label `label[1]` " +
                        "and properties `property[2]` = true, `property[3]` = 'hi', `property[4]` = [6, 4]" ) );
    }
}
