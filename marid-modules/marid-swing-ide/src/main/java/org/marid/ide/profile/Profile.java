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

package org.marid.ide.profile;

import org.marid.ide.components.ProfileManager;
import org.marid.ide.components.conf.ProfilePreferencesConfiguration;
import org.marid.itf.Named;
import org.marid.logging.LogSupport;
import org.marid.nio.FileUtils;

import javax.management.MBeanServerConnection;
import java.io.Closeable;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * @author Dmitry Ovchinnikov
 */
public class Profile implements Named, Closeable, LogSupport {

    protected final ProfileManager profileManager;
    protected final Path path;
    protected final ProfilePreferencesConfiguration configuration;

    public Profile(ProfileManager profileManager, Path path) {
        this.profileManager = profileManager;
        this.path = path;
        this.configuration = new ProfilePreferencesConfiguration(this);
    }

    public Path getClassesPath() {
        return path.resolve("classes");
    }

    public Path getContextPath() {
        return path.resolve("context");
    }

    public void clean() {
        for (final Path dir : Arrays.asList(getClassesPath(), getContextPath())) {
            try (final DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (final Path path : stream) {
                    FileUtils.remove(path);
                }
            } catch (IOException x) {
                log(WARNING, "Unable to clean {0}", x, dir);
            }
        }
    }

    public String getName() {
        return path.getFileName().toString();
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    @Override
    public void close() throws IOException {
    }

    public void start(Runnable init) {
    }

    public void start() {
        start(() -> {});
    }

    public void stop() {
    }

    public boolean isStarted() {
        return true;
    }

    @Override
    public String toString() {
        return getName();
    }

    public ProfilePreferencesConfiguration getConfiguration() {
        return configuration;
    }

    public MBeanServerConnection getConnection() {
        return ManagementFactory.getPlatformMBeanServer();
    }
}
