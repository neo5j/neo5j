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
package org.neo5j.graphdb.config;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This interface represents a setting group. One example can be group defined by a common prefix, such as
 * `dbms.connector.*`. The important aspect is that config keys can only be known after a config has been parsed.
 */
public interface SettingGroup<T> extends SettingValidator
{
    /**
     * Apply this setting group to the config and return all of its configured keys and their corresponding values.
     *
     * @param validConfig which can be examined
     * @return the map of this group's configured keys and values
     */
    Map<String,T> values( Map<String,String> validConfig );

    /**
     * @return a list of the settings this group contains
     */
    List<Setting<T>> settings( Map<String,String> params );

    /**
     *
     * @return true if this setting is deprecated, false otherwise
     */
    boolean deprecated();

    /**
     *
     * @return the key of the setting which replaces this when its deprecated, empty if not depricated
     */
    Optional<String> replacement();

    /**
     *
     * @return true if internal setting, false otherwise
     */
    boolean internal();

    /**
     *
     * @return the documented default value if it needs special documentation, empty if default value is good as is
     */
    Optional<String> documentedDefaultValue();

    /**
     *
     * @return description of which values are good
     */
    String valueDescription();

    /**
     *
     * @return description of setting, empty in case no description exists
     */
    Optional<String> description();
}
