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

import org.neo5j.consistency.checking.full.RecordProcessor;
import org.neo5j.consistency.report.ConsistencyReporter;
import org.neo5j.consistency.store.synthetic.LabelScanDocument;
import org.neo5j.kernel.api.labelscan.NodeLabelRange;

public class LabelScanDocumentProcessor extends RecordProcessor.Adapter<NodeLabelRange>
{
    private final ConsistencyReporter reporter;
    private final LabelScanCheck labelScanCheck;

    public LabelScanDocumentProcessor( ConsistencyReporter reporter, LabelScanCheck labelScanCheck )
    {
        this.reporter = reporter;
        this.labelScanCheck = labelScanCheck;
    }

    @Override
    public void process( NodeLabelRange nodeLabelRange )
    {
        reporter.forNodeLabelScan( new LabelScanDocument( nodeLabelRange ), labelScanCheck );
    }
}
