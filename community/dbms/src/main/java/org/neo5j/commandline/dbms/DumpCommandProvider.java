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
package org.neo5j.commandline.dbms;

import java.nio.file.Path;
import javax.annotation.Nonnull;

import org.neo5j.commandline.admin.AdminCommand;
import org.neo5j.commandline.admin.AdminCommandSection;
import org.neo5j.commandline.admin.OutsideWorld;
import org.neo5j.commandline.arguments.Arguments;
import org.neo5j.dbms.archive.Dumper;

public class DumpCommandProvider extends AdminCommand.Provider
{
    public DumpCommandProvider()
    {
        super( "dump" );
    }

    @Override
    @Nonnull
    public Arguments allArguments()
    {
        return DumpCommand.arguments();
    }

    @Override
    @Nonnull
    public String description()
    {
        return "Dump a database into a single-file archive. The archive can be used by the load command. " +
                "<destination-path> can be a file or directory (in which case a file called <database>.dump will " +
                "be created). It is not possible to dump a database that is mounted in a running Neo5j server.";
    }

    @Override
    @Nonnull
    public String summary()
    {
        return "Dump a database into a single-file archive.";
    }

    @Override
    @Nonnull
    public AdminCommandSection commandSection()
    {
        return OffineBackupCommandSection.instance();
    }

    @Override
    @Nonnull
    public AdminCommand create( Path homeDir, Path configDir, OutsideWorld outsideWorld )
    {
        return new DumpCommand( homeDir, configDir, new Dumper() );
    }
}
