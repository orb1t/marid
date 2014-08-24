/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.ide.swing;

import org.marid.ide.base.Ide;
import org.marid.logging.LogSupport;
import org.marid.pref.SysPrefSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeImpl implements Ide, SysPrefSupport, LogSupport {

    @Autowired
    private GenericApplicationContext applicationContext;

    @Override
    public Path getProfilesDir() {
        try {
            final Path defaultDir = Paths.get(System.getProperty("user.home"), "marid", "profiles");
            final Path path = Paths.get(SYSPREFS.get("profilesDir", defaultDir.toString()));
            if (!Files.isDirectory(path)) {
                Files.createDirectories(path);
            }
            return path;
        } catch (Exception x) {
            warning("Unable to get profiles directory", x);
            return Paths.get(System.getProperty("user.dir"));
        }
    }

    @Override
    public void exit() {
        applicationContext.close();
        System.exit(0);
    }

    @Override
    public String getName() {
        return "ide";
    }
}
