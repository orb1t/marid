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

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.marid.groovy.GroovyRuntime;
import org.marid.ide.Ide;
import org.marid.itf.Named;
import org.marid.logging.LogSupport;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class Profile implements Named, Closeable, LogSupport {

    protected final CompilerConfiguration compilerConfiguration = GroovyRuntime.newCompilerConfiguration(c -> {
    });
    protected final GroovyClassLoader classLoader = new GroovyClassLoader();
    protected final String name;

    public Profile(String name) {
        this.name = name;
        this.classLoader.setShouldRecompile(true);
    }

    public String getName() {
        return name;
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
        classLoader.close();
    }

    public void update() {
        classLoader.clearCache();
        final Path classesDir = Ide.getProfilesDir().resolve("classes");
        try (final Stream<Path> stream = Files.walk(classesDir)) {

        } catch (Exception x) {
            warning("Unable to enumerate profiles directory", x);
        }
    }
}
