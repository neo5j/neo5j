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
package org.neo5j.consistency.checking.full;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import org.neo5j.consistency.ConsistencyCheckSettings;
import org.neo5j.kernel.configuration.Config;

public class CheckConsistencyConfig
{
    private final boolean checkGraph;
    private final boolean checkIndexes;
    private final boolean checkLabelScanStore;
    private final boolean checkPropertyOwners;

    public CheckConsistencyConfig( Config tuningConfiguration )
    {
        this( tuningConfiguration.get( ConsistencyCheckSettings.consistency_check_graph ),
                tuningConfiguration.get( ConsistencyCheckSettings.consistency_check_indexes ),
                tuningConfiguration.get( ConsistencyCheckSettings.consistency_check_label_scan_store ),
                tuningConfiguration.get( ConsistencyCheckSettings.consistency_check_property_owners ) );
    }

    public CheckConsistencyConfig( boolean checkGraph,
            boolean checkIndexes,
            boolean checkLabelScanStore,
            boolean checkPropertyOwners )
    {
        this.checkGraph = checkGraph;
        this.checkIndexes = checkIndexes;
        this.checkLabelScanStore = checkLabelScanStore;
        this.checkPropertyOwners = checkPropertyOwners;
    }

    public boolean isCheckGraph()
    {
        return checkGraph;
    }

    public boolean isCheckIndexes()
    {
        return checkIndexes;
    }

    public boolean isCheckLabelScanStore()
    {
        return checkLabelScanStore;
    }

    public boolean isCheckPropertyOwners()
    {
        return checkPropertyOwners;
    }

    @Override
    public boolean equals( Object o )
    {
        return EqualsBuilder.reflectionEquals( this, o );
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode( this );
    }
}
