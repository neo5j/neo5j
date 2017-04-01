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
package org.neo5j.configuration;

import java.util.Optional;
import javax.annotation.Nonnull;

/**
 * A configuration option with its active value.
 */
public class ConfigValue
{
    private final String name;
    private final Optional<String> description;
    private final Optional<String> documentedDefaultValue;
    private final Optional<?> value;
    private final String valueDescription;
    private final boolean internal;
    private final boolean deprecated;
    private final Optional<String> replacement;

    public ConfigValue( @Nonnull String name, @Nonnull Optional<String> description,
            @Nonnull Optional<String> documentedDefaultValue, @Nonnull Optional<?> value,
            @Nonnull String valueDescription, boolean internal, boolean deprecated,
            @Nonnull Optional<String> replacement )
    {
        this.name = name;
        this.description = description;
        this.documentedDefaultValue = documentedDefaultValue;
        this.value = value;
        this.valueDescription = valueDescription;
        this.internal = internal;
        this.deprecated = deprecated;
        this.replacement = replacement;
    }

    @Nonnull
    public String name()
    {
        return name;
    }

    @Nonnull
    public Optional<String> description()
    {
        return description;
    }

    @Nonnull
    public Optional<?> value()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return value.map( Object::toString ).orElse( "null" );
    }

    public boolean deprecated()
    {
        return deprecated;
    }

    @Nonnull
    public Optional<String> replacement()
    {
        return replacement;
    }

    public boolean internal()
    {
        return internal;
    }

    @Nonnull
    public Optional<String> documentedDefaultValue()
    {
        return documentedDefaultValue;
    }

    @Nonnull
    public String valueDescription()
    {
        return valueDescription;
    }
}