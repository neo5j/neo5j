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
package org.neo5j.io.pagecache.impl.muninn;

import java.io.File;

final class FileMapping
{
    public volatile FileMapping next;
    public final File file;
    public final MuninnPagedFile pagedFile;

    FileMapping( File file, MuninnPagedFile pagedFile )
    {
        this.file = file;
        this.pagedFile = pagedFile;
    }

    @Override
    public String toString()
    {
        return String.format( "FileMapping[fname = %s, refCount = %s] :: %s",
                file, pagedFile.getRefCount(), next );
    }
}