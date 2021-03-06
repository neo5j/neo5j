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
package org.neo5j.causalclustering;

import java.io.File;
import java.util.UUID;

import org.neo5j.causalclustering.core.CausalClusteringSettings;
import org.neo5j.causalclustering.core.consensus.ConsensusModule;
import org.neo5j.causalclustering.core.consensus.RaftMessages;
import org.neo5j.causalclustering.core.replication.ProgressTrackerImpl;
import org.neo5j.causalclustering.core.replication.RaftReplicator;
import org.neo5j.causalclustering.core.replication.session.GlobalSession;
import org.neo5j.causalclustering.core.replication.session.GlobalSessionTrackerState;
import org.neo5j.causalclustering.core.replication.session.LocalSessionPool;
import org.neo5j.causalclustering.core.state.storage.DurableStateStorage;
import org.neo5j.causalclustering.helper.ExponentialBackoffStrategy;
import org.neo5j.causalclustering.identity.MemberId;
import org.neo5j.causalclustering.messaging.Outbound;
import org.neo5j.io.fs.FileSystemAbstraction;
import org.neo5j.kernel.configuration.Config;
import org.neo5j.kernel.impl.factory.PlatformModule;
import org.neo5j.kernel.lifecycle.LifeSupport;
import org.neo5j.logging.LogProvider;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ReplicationModule
{
    public static final String SESSION_TRACKER_NAME = "session-tracker";

    private final RaftReplicator replicator;
    private final ProgressTrackerImpl progressTracker;
    private final SessionTracker sessionTracker;

    public ReplicationModule( MemberId myself, PlatformModule platformModule, Config config,
            ConsensusModule consensusModule, Outbound<MemberId,RaftMessages.RaftMessage> outbound,
            File clusterStateDirectory, FileSystemAbstraction fileSystem, LogProvider logProvider )
    {
        LifeSupport life = platformModule.life;

        DurableStateStorage<GlobalSessionTrackerState> sessionTrackerStorage;
        sessionTrackerStorage = life.add( new DurableStateStorage<>( fileSystem, clusterStateDirectory,
                SESSION_TRACKER_NAME, new GlobalSessionTrackerState.Marshal( new MemberId.Marshal() ),
                config.get( CausalClusteringSettings.global_session_tracker_state_size ), logProvider ) );

        sessionTracker = new SessionTracker( sessionTrackerStorage );

        GlobalSession myGlobalSession = new GlobalSession( UUID.randomUUID(), myself );
        LocalSessionPool sessionPool = new LocalSessionPool( myGlobalSession );
        progressTracker = new ProgressTrackerImpl( myGlobalSession );

        ExponentialBackoffStrategy retryStrategy = new ExponentialBackoffStrategy( 10, 60, SECONDS );
        replicator = life.add( new RaftReplicator( consensusModule.raftMachine(), myself, outbound, sessionPool,
            progressTracker, retryStrategy, platformModule.availabilityGuard, logProvider ) );
    }

    public RaftReplicator getReplicator()
    {
        return replicator;
    }

    public ProgressTrackerImpl getProgressTracker()
    {
        return progressTracker;
    }

    public SessionTracker getSessionTracker()
    {
        return sessionTracker;
    }
}
