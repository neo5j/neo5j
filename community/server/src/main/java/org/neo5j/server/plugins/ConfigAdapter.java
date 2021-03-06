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
package org.neo5j.server.plugins;

import org.apache.commons.configuration.AbstractConfiguration;

import java.util.Iterator;

import org.neo5j.kernel.configuration.Config;

import static org.neo5j.helpers.collection.MapUtil.stringMap;

public class ConfigAdapter extends AbstractConfiguration
{
    private final Config config;

    public ConfigAdapter( Config config )
    {
        this.config = config;
    }

    @Override
    public boolean isEmpty()
    {
        // non-null config is always non-empty as some properties have default values
        return config == null;
    }

    @Override
    public boolean containsKey( String key )
    {
        return config.getConfiguredSettingKeys().contains( key );
    }

    @Override
    public Object getProperty( String key )
    {
        return config.getValue( key ).orElse( null );
    }

    @Override
    public Iterator<String> getKeys()
    {
        return config.getConfigValues().keySet().iterator();
    }

    @Override
    protected void addPropertyDirect( String key, Object value )
    {
        config.augment( stringMap( key, value.toString() ) );
    }
}
