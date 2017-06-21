/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.ide.common;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class Directories {

    private final Path userHome;
    private final Path marid;
    private final Path profiles;

    public Directories() {
        userHome = Paths.get(System.getProperty("user.home"));
        marid = userHome.resolve("marid");
        profiles = marid.resolve("profiles");
    }

    @PostConstruct
    private void init() throws IOException {
        Files.createDirectories(profiles);
    }

    public Path getUserHome() {
        return userHome;
    }

    public Path getMarid() {
        return marid;
    }

    public Path getProfiles() {
        return profiles;
    }
}
