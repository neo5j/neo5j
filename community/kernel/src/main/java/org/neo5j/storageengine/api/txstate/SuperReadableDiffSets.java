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
package org.neo5j.storageengine.api.txstate;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;

import org.neo5j.collection.primitive.PrimitiveIntIterator;
import org.neo5j.collection.primitive.PrimitiveLongIterator;
import org.neo5j.kernel.api.exceptions.schema.ConstraintValidationException;
import org.neo5j.kernel.api.exceptions.schema.CreateConstraintFailureException;

/**
 * Super class of diff sets where use of {@link PrimitiveLongIterator} can be parameterized
 * to a specific subclass instead.
 */
public interface SuperReadableDiffSets<T,LONGITERATOR extends PrimitiveLongIterator>
{
    boolean isAdded( T elem );

    boolean isRemoved( T elem );

    Set<T> getAdded();

    Set<T> getRemoved();

    boolean isEmpty();

    Iterator<T> apply( Iterator<T> source );

    int delta();

    LONGITERATOR augment( LONGITERATOR source );

    PrimitiveIntIterator augment( PrimitiveIntIterator source );

    LONGITERATOR augmentWithRemovals( LONGITERATOR source );

    SuperReadableDiffSets<T,LONGITERATOR> filterAdded( Predicate<T> addedFilter );

    void accept( DiffSetsVisitor<T> visitor ) throws ConstraintValidationException, CreateConstraintFailureException;
}
