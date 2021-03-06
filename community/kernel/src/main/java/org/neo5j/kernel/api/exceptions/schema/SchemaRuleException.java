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
package org.neo5j.kernel.api.exceptions.schema;

import org.neo5j.kernel.api.TokenNameLookup;
import org.neo5j.kernel.api.exceptions.Status;
import org.neo5j.kernel.api.schema_new.SchemaDescriptor;
import org.neo5j.kernel.api.schema_new.SchemaUtil;
import org.neo5j.storageengine.api.schema.SchemaRule;

import static java.lang.String.format;

/**
 * Represent something gone wrong related to SchemaRules
 */
class SchemaRuleException extends SchemaKernelException
{
    protected final SchemaDescriptor descriptor;
    protected final String messageTemplate;
    protected final SchemaRule.Kind kind;

    /**
     * @param messageTemplate Template for String.format. Must match two strings representing the schema kind and the
     *                        descriptor
     */
    protected SchemaRuleException( Status status, String messageTemplate, SchemaRule.Kind kind,
            SchemaDescriptor descriptor )
    {
        super( status, format( messageTemplate, kind.userString().toLowerCase(),
                descriptor.userDescription( SchemaUtil.idTokenNameLookup ) ) );
        this.descriptor = descriptor;
        this.messageTemplate = messageTemplate;
        this.kind = kind;
    }

    @Override
    public String getUserMessage( TokenNameLookup tokenNameLookup )
    {
        return format( messageTemplate, kind.userString().toLowerCase(), descriptor.userDescription( tokenNameLookup ) );
    }
}
