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
package org.neo5j.kernel.impl.api.index.updater;

import java.io.IOException;

import org.neo5j.collection.primitive.PrimitiveLongSet;
import org.neo5j.kernel.api.exceptions.index.IndexEntryConflictException;
import org.neo5j.kernel.api.index.IndexEntryUpdate;
import org.neo5j.kernel.api.index.IndexUpdater;

public abstract class DelegatingIndexUpdater implements IndexUpdater
{
    protected final IndexUpdater delegate;

    public DelegatingIndexUpdater( IndexUpdater delegate )
    {
        this.delegate = delegate;
    }

    @Override
    public void process( IndexEntryUpdate update ) throws IOException, IndexEntryConflictException
    {
        delegate.process( update );
    }

    @Override
    public void remove( PrimitiveLongSet nodeIds ) throws IOException
    {
        delegate.remove( nodeIds );
    }
}
