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
package org.neo5j.kernel.impl.transaction.log.stresstest;

import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.function.BooleanSupplier;

import org.neo5j.io.fs.DefaultFileSystemAbstraction;
import org.neo5j.io.fs.FileSystemAbstraction;
import org.neo5j.kernel.impl.transaction.log.PhysicalLogFile;
import org.neo5j.kernel.impl.transaction.log.PhysicalLogFiles;
import org.neo5j.kernel.impl.transaction.log.PhysicalLogVersionedStoreChannel;
import org.neo5j.kernel.impl.transaction.log.ReadAheadLogChannel;
import org.neo5j.kernel.impl.transaction.log.ReadableLogChannel;
import org.neo5j.kernel.impl.transaction.log.ReaderLogVersionBridge;
import org.neo5j.kernel.impl.transaction.log.entry.LogEntry;
import org.neo5j.kernel.impl.transaction.log.entry.LogEntryByteCodes;
import org.neo5j.kernel.impl.transaction.log.entry.LogEntryReader;
import org.neo5j.kernel.impl.transaction.log.entry.OnePhaseCommit;
import org.neo5j.kernel.impl.transaction.log.entry.VersionAwareLogEntryReader;
import org.neo5j.kernel.impl.transaction.log.stresstest.workload.Runner;
import org.neo5j.test.rule.TestDirectory;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.neo5j.function.Suppliers.untilTimeExpired;

public class TransactionAppenderStressTest
{
    @Rule
    public final TestDirectory directory = TestDirectory.testDirectory( );

    @Test
    public void concurrentTransactionAppendingTest() throws Exception
    {
        int threads = 10;
        File workingDirectory = directory.directory( "work" );
        Callable<Long> runner = new Builder()
                .with( untilTimeExpired( 10, SECONDS ) )
                .withWorkingDirectory( workingDirectory )
                .withNumThreads( threads )
                .build();

        long appendedTxs = runner.call();

        assertEquals( new TransactionIdChecker( workingDirectory ).parseAllTxLogs(), appendedTxs );
    }

    public static class Builder
    {
        private BooleanSupplier condition;
        private File workingDirectory;
        private int threads;

        public Builder with( BooleanSupplier condition )
        {
            this.condition = condition;
            return this;
        }

        public Builder withWorkingDirectory( File workingDirectory )
        {
            this.workingDirectory = workingDirectory;
            return this;
        }

        public Builder withNumThreads( int threads )
        {
            this.threads = threads;
            return this;
        }

        public Callable<Long> build()
        {
            return new Runner( workingDirectory, condition, threads );
        }
    }

    public static class TransactionIdChecker
    {
        private final File workingDirectory;

        public TransactionIdChecker( File workingDirectory )
        {
            this.workingDirectory = workingDirectory;
        }

        public long parseAllTxLogs() throws IOException
        {
            long txId = -1;
            try ( FileSystemAbstraction fs = new DefaultFileSystemAbstraction();
                  ReadableLogChannel channel = openLogFile( fs, 0 ) )
            {
                LogEntryReader<ReadableLogChannel> reader = new VersionAwareLogEntryReader<>();
                LogEntry logEntry = reader.readLogEntry( channel );
                for ( ; logEntry != null; logEntry = reader.readLogEntry( channel ) )
                {
                    if ( logEntry.getType() == LogEntryByteCodes.TX_1P_COMMIT )
                    {
                        txId = logEntry.<OnePhaseCommit>as().getTxId();
                    }
                }
            }
            return txId;
        }

        private ReadableLogChannel openLogFile( FileSystemAbstraction fs, int version ) throws IOException
        {
            PhysicalLogFiles logFiles = new PhysicalLogFiles( workingDirectory, fs );
            PhysicalLogVersionedStoreChannel channel = PhysicalLogFile.openForVersion( logFiles, fs, version, false );
            return new ReadAheadLogChannel( channel, new ReaderLogVersionBridge( fs, logFiles ) );
        }
    }
}
