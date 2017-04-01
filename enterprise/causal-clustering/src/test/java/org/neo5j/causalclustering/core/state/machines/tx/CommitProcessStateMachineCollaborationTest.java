/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo5j.
 *
 * Neo5j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo5j.causalclustering.core.state.machines.tx;

import org.junit.Test;

import org.neo5j.causalclustering.core.replication.DirectReplicator;
import org.neo5j.causalclustering.core.state.machines.locks.ReplicatedLockTokenRequest;
import org.neo5j.causalclustering.core.state.machines.locks.ReplicatedLockTokenStateMachine;
import org.neo5j.kernel.api.exceptions.TransactionFailureException;
import org.neo5j.kernel.impl.api.TransactionCommitProcess;
import org.neo5j.kernel.impl.api.TransactionToApply;
import org.neo5j.kernel.impl.transaction.log.PhysicalTransactionRepresentation;
import org.neo5j.logging.NullLogProvider;

import static junit.framework.TestCase.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo5j.kernel.impl.transaction.tracing.CommitEvent.NULL;
import static org.neo5j.storageengine.api.TransactionApplicationMode.EXTERNAL;

public class CommitProcessStateMachineCollaborationTest
{
    @Test
    public void shouldFailTransactionIfLockSessionChanges() throws Exception
    {
        // given
        int initialLockSessionId = 23;
        TransactionToApply transactionToApply = new TransactionToApply( physicalTx( initialLockSessionId ) );

        int finalLockSessionId = 24;
        TransactionCommitProcess localCommitProcess = mock( TransactionCommitProcess.class );
        ReplicatedTransactionStateMachine stateMachine = new ReplicatedTransactionStateMachine(
                lockState( finalLockSessionId ), 16, NullLogProvider.getInstance() );
        stateMachine.installCommitProcess( localCommitProcess, -1L );

        DirectReplicator<ReplicatedTransaction> replicator = new DirectReplicator<>( stateMachine );
        ReplicatedTransactionCommitProcess commitProcess = new ReplicatedTransactionCommitProcess( replicator );

        // when
        try
        {
            commitProcess.commit( transactionToApply, NULL, EXTERNAL );
            fail( "Should have thrown exception." );
        }
        catch ( TransactionFailureException e )
        {
            // expected
        }
    }

    private PhysicalTransactionRepresentation physicalTx( int lockSessionId )
    {
        PhysicalTransactionRepresentation physicalTx = mock( PhysicalTransactionRepresentation.class );
        when( physicalTx.getLockSessionId() ).thenReturn( lockSessionId );
        return physicalTx;
    }

    private ReplicatedLockTokenStateMachine lockState( int lockSessionId )
    {
        ReplicatedLockTokenStateMachine lockState = mock( ReplicatedLockTokenStateMachine.class );
        when( lockState.currentToken() ).thenReturn( new ReplicatedLockTokenRequest( null, lockSessionId ) );
        return lockState;
    }
}