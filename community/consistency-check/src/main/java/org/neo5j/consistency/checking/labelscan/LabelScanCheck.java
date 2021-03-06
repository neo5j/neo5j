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
package org.neo5j.consistency.checking.labelscan;

import org.neo5j.consistency.checking.CheckerEngine;
import org.neo5j.consistency.checking.RecordCheck;
import org.neo5j.consistency.checking.full.NodeInUseWithCorrectLabelsCheck;
import org.neo5j.consistency.report.ConsistencyReport;
import org.neo5j.consistency.store.RecordAccess;
import org.neo5j.consistency.store.synthetic.LabelScanDocument;
import org.neo5j.kernel.api.labelscan.NodeLabelRange;

public class LabelScanCheck implements RecordCheck<LabelScanDocument, ConsistencyReport.LabelScanConsistencyReport>
{
    @Override
    public void check( LabelScanDocument record, CheckerEngine<LabelScanDocument,
            ConsistencyReport.LabelScanConsistencyReport> engine, RecordAccess records )
    {
        NodeLabelRange range = record.getNodeLabelRange();
        for ( long nodeId : range.nodes() )
        {
            engine.comparativeCheck( records.node( nodeId ),
                    new NodeInUseWithCorrectLabelsCheck<>( record.getNodeLabelRange().labels( nodeId ), true ) );
        }
    }
}
