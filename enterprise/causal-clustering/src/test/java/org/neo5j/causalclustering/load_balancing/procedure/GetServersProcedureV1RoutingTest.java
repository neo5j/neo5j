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
package org.neo5j.causalclustering.load_balancing.procedure;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.neo5j.causalclustering.core.consensus.LeaderLocator;
import org.neo5j.causalclustering.discovery.CoreServerInfo;
import org.neo5j.causalclustering.discovery.CoreTopology;
import org.neo5j.causalclustering.discovery.CoreTopologyService;
import org.neo5j.causalclustering.discovery.ReadReplicaTopology;
import org.neo5j.causalclustering.identity.ClusterId;
import org.neo5j.causalclustering.identity.MemberId;
import org.neo5j.kernel.configuration.Config;

import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertFalse;
import static org.junit.runners.Parameterized.Parameters;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.neo5j.causalclustering.discovery.TestTopology.adressesForCore;
import static org.neo5j.causalclustering.identity.RaftTestMember.member;
import static org.neo5j.helpers.collection.Iterators.asList;
import static org.neo5j.logging.NullLogProvider.getInstance;

@RunWith( Parameterized.class )
public class GetServersProcedureV1RoutingTest
{
    @Parameters
    public static Collection<Object> data()
    {
        return Arrays.asList( 1, 2 );
    } //the write endpoints are always index 0

    @Parameter
    public int serverClass;

    private ClusterId clusterId = new ClusterId( UUID.randomUUID() );
    private Config config = Config.defaults();

    @Test
    public void shouldReturnEndpointsInDifferentOrders() throws Exception
    {
        // given
        final CoreTopologyService coreTopologyService = mock( CoreTopologyService.class );

        LeaderLocator leaderLocator = mock( LeaderLocator.class );
        when( leaderLocator.getLeader() ).thenReturn( member( 0 ) );

        Map<MemberId,CoreServerInfo> coreMembers = new HashMap<>();
        coreMembers.put( member( 0 ), adressesForCore( 0 ) );
        coreMembers.put( member( 1 ), adressesForCore( 1 ) );
        coreMembers.put( member( 2 ), adressesForCore( 2 ) );

        final CoreTopology clusterTopology = new CoreTopology( clusterId, false, coreMembers );
        when( coreTopologyService.coreServers() ).thenReturn( clusterTopology );
        when( coreTopologyService.readReplicas() ).thenReturn( new ReadReplicaTopology( emptyMap() ) );

        final LegacyGetServersProcedure proc =
                new LegacyGetServersProcedure( coreTopologyService, leaderLocator, config, getInstance() );

        // when
        Object[] endpoints = getEndpoints( proc );

        //then
        Object[] endpointsInDifferentOrder = getEndpoints( proc );
        for ( int i = 0; i < 100; i++ )
        {
            if ( Arrays.deepEquals( endpointsInDifferentOrder, endpoints ) )
            {
                endpointsInDifferentOrder = getEndpoints( proc );
            }
            else
            {
                //Different order of servers, no need to retry.
                break;
            }
        }
        assertFalse( Arrays.deepEquals( endpoints, endpointsInDifferentOrder ) );
    }

    private Object[] getEndpoints( LegacyGetServersProcedure proc )
            throws org.neo5j.kernel.api.exceptions.ProcedureException
    {
        List<Object[]> results = asList( proc.apply( null, new Object[0] ) );
        Object[] rows = results.get( 0 );
        List<Map<String,Object[]>> servers = (List<Map<String,Object[]>>) rows[1];
        Map<String,Object[]> endpoints = servers.get( serverClass );
        return endpoints.get( "addresses" );
    }
}
