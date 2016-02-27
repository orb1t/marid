/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.ide.beaned;

import javafx.scene.layout.BorderPane;
import org.marid.ide.project.ProjectProfile;
import org.marid.logging.LogSupport;
import org.marid.misc.Calls;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class BeanEditorPane extends BorderPane implements LogSupport {

    final ProjectProfile profile;
    final URLClassLoader classLoader;

    @Inject
    public BeanEditorPane(ProjectProfile profile) {
        this.profile = profile;
        this.classLoader = classLoader(profile);
    }

    private URLClassLoader classLoader(ProjectProfile profile) {
        final Path lib = profile.getTarget().resolve("lib");
        final URL[] urls;
        if (Files.isDirectory(lib)) {
            final File[] files = lib.toFile().listFiles((dir, name) -> name.endsWith(".jar"));
            urls = Stream.of(files).map(f -> Calls.call(() -> f.toURI().toURL())).toArray(URL[]::new);
        } else {
            urls = new URL[0];
        }
        return new URLClassLoader(urls);
    }
}
