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
import org.marid.bd.schema.SchemaModel;
import org.marid.beans.MaridBeans;
import org.marid.groovy.GroovyRuntime;
import org.marid.io.SimpleWriter;
import org.marid.itf.Named;
import org.marid.logging.LogSupport;
import org.marid.logging.SimpleHandler;
import org.marid.nio.FileUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class Profile implements Named, Closeable, LogSupport {

    protected final List<Consumer<String>> outputConsumers = new CopyOnWriteArrayList<>();
    protected final List<Consumer<LogRecord>> logConsumers = new CopyOnWriteArrayList<>();
    protected final CompilerConfiguration compilerConfiguration = GroovyRuntime.newCompilerConfiguration(c -> {
        c.setRecompileGroovySource(true);
        c.setOutput(new PrintWriter(new SimpleWriter((w, s) -> outputConsumers.forEach(cs -> cs.accept(s))), true));
    });
    protected final Path path;
    protected final GroovyShell shell;
    protected final ThreadGroup threadGroup;

    protected volatile AnnotationConfigApplicationContext applicationContext;

    public Profile(Path path) {
        this.path = path;
        this.threadGroup = new ThreadGroup(getName());
        this.shell = GroovyRuntime.newShell(compilerConfiguration, (l, s) -> {
            l.setShouldRecompile(true);
            l.setDefaultAssertionStatus(true);
            Files.createDirectories(getClassesPath());
            Files.createDirectories(getContextPath());
            l.addURL(getClassesPath().toUri().toURL());
        });
    }

    public void saveContextClass(SchemaModel schemaModel) {
        final Path metaPath = getContextPath().resolve(schemaModel.getSchema().getName() + ".xml");
        try (final OutputStream outputStream = Files.newOutputStream(metaPath)) {
            MaridBeans.write(outputStream, schemaModel);
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    public void addOutputConsumer(Consumer<String> outputConsumer) {
        outputConsumers.add(outputConsumer);
    }

    public void removeOutputConsumer(Consumer<String> outputConsumer) {
        outputConsumers.remove(outputConsumer);
    }

    public void addLogConsumer(Consumer<LogRecord> logRecordConsumer) {
        logConsumers.add(logRecordConsumer);
    }

    public void removeLogRecordConsumer(Consumer<LogRecord> logRecordConsumer) {
        logConsumers.remove(logRecordConsumer);
    }

    public Path getClassesPath() {
        return path.resolve("classes");
    }

    public Path getContextPath() {
        return path.resolve("context");
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
        try {
            shell.getClassLoader().close();
        } finally {
            Files.walkFileTree(path, FileUtils.RECURSIVE_CLEANER);
        }
    }

    public void update() {
        shell.resetLoadedClasses();
    }

    public synchronized void start() {
        if (applicationContext != null) {
            return;
        }
        applicationContext = new AnnotationConfigApplicationContext();
        applicationContext.setClassLoader(shell.getClassLoader());
        try (final DirectoryStream<Path> stream = Files.newDirectoryStream(getContextPath(), "*.groovy")) {
            for (final Path file : stream) {
                final String className = FileUtils.fileNameWithoutExtension(file.getFileName().toString());
                try {
                    applicationContext.register(shell.getClassLoader().loadClass(className, true, true, true));
                } catch (Exception x) {
                    warning("Unable to load {0}", x, className);
                }
            }
        } catch (IOException x) {
            warning("Unable to stream {0}", x, path);
        }
        applicationContext.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                info("Context {0} closed at {1}", event.getSource(), Instant.ofEpochMilli(event.getTimestamp()));
                applicationContext = null;
            } else if (event instanceof ContextStartedEvent) {
                info("Context {0} started at {1}", event.getSource(), Instant.ofEpochMilli(event.getTimestamp()));
                for (final LogSupport logSupport : applicationContext.getBeansOfType(LogSupport.class).values()) {
                    logSupport.logger().addHandler(new SimpleHandler((h, r) -> logConsumers.forEach(c -> c.accept(r))));
                }
            }
        });
        applicationContext.refresh();
        applicationContext.start();
    }

    public synchronized void stop() {
        if (applicationContext == null) {
            return;
        }
        applicationContext.close();
    }

    @Override
    public String toString() {
        return getName();
    }
}
