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
package org.neo5j.kernel.impl.transaction.log.pruning;

import java.io.File;

import org.neo5j.io.fs.FileSystemAbstraction;
import org.neo5j.kernel.impl.transaction.log.LogFileInformation;

public final class FileSizeThreshold implements Threshold
{
    private final FileSystemAbstraction fileSystem;
    private final long maxSize;

    private long currentSize;

    public FileSizeThreshold( FileSystemAbstraction fileSystem, long maxSize )
    {
        this.fileSystem = fileSystem;
        this.maxSize = maxSize;
    }

    @Override
    public void init()
    {
        currentSize = 0;
    }

    @Override
    public boolean reached( File file, long version, LogFileInformation source )
    {
        currentSize += fileSystem.getFileSize( file );
        return currentSize >= maxSize;
    }
}