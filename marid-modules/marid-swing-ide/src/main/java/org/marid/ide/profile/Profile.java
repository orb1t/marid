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

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.marid.groovy.GroovyRuntime;
import org.marid.io.SimpleWriter;
import org.marid.itf.Named;
import org.marid.logging.LogSupport;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.context.support.GenericGroovyApplicationContext;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class Profile implements Named, Closeable, LogSupport {

    protected final List<Consumer<String>> outputConsumers = new CopyOnWriteArrayList<>();
    protected final CompilerConfiguration compilerConfiguration = GroovyRuntime.newCompilerConfiguration(c -> {
        c.setRecompileGroovySource(true);
        c.setOutput(new PrintWriter(new SimpleWriter((w, s) -> outputConsumers.forEach(cs -> cs.accept(s))), true));
    });
    protected final Path path;
    protected final GroovyShell shell;
    protected final ThreadGroup threadGroup;

    protected volatile GenericApplicationContext applicationContext;

    public Profile(Path path) {
        this.path = path;
        this.threadGroup = new ThreadGroup(getName());
        this.shell = GroovyRuntime.newShell(compilerConfiguration, (l, s) -> {
            l.setShouldRecompile(true);
            l.setDefaultAssertionStatus(true);
            Files.createDirectories(getClassesPath());
            l.addURL(getClassesPath().toUri().toURL());
        });
    }

    public void addOutputConsumer(Consumer<String> outputConsumer) {
        outputConsumers.add(outputConsumer);
    }

    public void removeOutputConsumer(Consumer<String> outputConsumer) {
        outputConsumers.remove(outputConsumer);
    }

    public Path getClassesPath() {
        return path.resolve("classes");
    }

    public String getName() {
        return path.getFileName().toString();
    }

    public Class<?> loadClass(String name) {
        try {
            return shell.getClassLoader().loadClass(name);
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
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
        shell.getClassLoader().close();
    }

    public void update() {
        shell.resetLoadedClasses();
    }

    public synchronized void start() {
        if (applicationContext != null) {
            return;
        }
        applicationContext = new GenericGroovyApplicationContext();
        applicationContext.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                info("Context {0} closed at {1}", event.getSource(), Instant.ofEpochMilli(event.getTimestamp()));
                applicationContext = null;
            } else if (event instanceof ContextStartedEvent) {
                info("Context {0} started at {1}", event.getSource(), Instant.ofEpochMilli(event.getTimestamp()));
            }
        });
        applicationContext.start();
    }

    public synchronized void stop() {
        if (applicationContext == null) {
            return;
        }
        try (final GenericApplicationContext ctx = applicationContext) {
            info("Closing context: {0}", ctx);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
