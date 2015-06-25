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
import org.marid.functions.SafeFunction;
import org.marid.groovy.GroovyRuntime;
import org.marid.ide.components.ProfileManager;
import org.marid.ide.log.LoggingPostProcessor;
import org.marid.io.SimpleWriter;
import org.marid.itf.Named;
import org.marid.logging.LogSupport;
import org.marid.nio.FileUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;

import javax.management.MBeanServerConnection;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.time.Instant.ofEpochMilli;

/**
 * @author Dmitry Ovchinnikov
 */
public class Profile implements Named, Closeable, LogSupport {

    protected final ProfileManager profileManager;
    protected final List<ApplicationListener<ApplicationEvent>> applicationListeners = new CopyOnWriteArrayList<>();
    protected final StringWriter compilerOutput = new StringWriter();
    protected final CompilerConfiguration compilerConfiguration = GroovyRuntime.newCompilerConfiguration(c -> {
        c.setRecompileGroovySource(true);
        c.setOutput(new PrintWriter(new SimpleWriter((w, s) -> compilerOutput.write(s)), true));
    });
    protected final Path path;
    protected final GroovyShell shell;
    protected final ThreadGroup threadGroup;
    protected final ExecutorService executor;

    protected AnnotationConfigApplicationContext applicationContext;

    public Profile(ProfileManager profileManager, Path path) {
        this.profileManager = profileManager;
        this.path = path;
        this.threadGroup = new ThreadGroup(getName());
        this.shell = GroovyRuntime.newShell(compilerConfiguration, (l, s) -> {
            l.setShouldRecompile(true);
            l.setDefaultAssertionStatus(true);
            Files.createDirectories(getClassesPath());
            Files.createDirectories(getContextPath());
            l.addURL(getClassesPath().toUri().toURL());
        });
        this.executor = Executors.newSingleThreadExecutor(r -> {
            final Thread thread = new Thread(threadGroup, r);
            thread.setDaemon(true);
            thread.setContextClassLoader(shell.getClassLoader());
            return thread;
        });
    }

    public StringBuffer getCompilerOutputBuffer() {
        return compilerOutput.getBuffer();
    }

    public void clearCompilerOutputBuffer() {
        synchronized (compilerOutput.getBuffer()) {
            compilerOutput.getBuffer().setLength(0);
            compilerOutput.getBuffer().trimToSize();
        }
    }

    public void addApplicationListener(ApplicationListener<ApplicationEvent> applicationListener) {
        applicationListeners.add(applicationListener);
    }

    public void removeApplicationListener(ApplicationListener<ApplicationEvent> applicationListener) {
        applicationListeners.remove(applicationListener);
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
            executor.shutdown();
            Files.walkFileTree(path, FileUtils.RECURSIVE_CLEANER);
        }
    }

    public void update() {
        shell.resetLoadedClasses();
    }

    public void start(Runnable init) {
        try {
            executor.submit(() -> {
                if (applicationContext != null) {
                    return;
                }
                init.run();
                applicationContext = new AnnotationConfigApplicationContext();
                applicationContext.addApplicationListener(event -> {
                    applicationListeners.forEach(l -> {
                        try {
                            l.onApplicationEvent(event);
                        } catch (Exception x) {
                            log(WARNING, "Unable to process {0}", x, event);
                        }
                    });
                    if (event instanceof ContextClosedEvent) {
                        log(INFO, "Context {0} closed at {1}", event.getSource(), ofEpochMilli(event.getTimestamp()));
                        applicationContext = null;
                    } else if (event instanceof ContextStartedEvent) {
                        final MBeanServerConnection connection = applicationContext.getBean(MBeanServerConnection.class);
                        profileManager.getConnectionManager().registerConnection(getName(), connection);
                    }
                });
                applicationContext.setClassLoader(shell.getClassLoader());
                try (final Stream<Path> stream = Files.walk(getClassesPath())) {
                    stream.filter(p -> p.getFileName().toString().endsWith(".groovy")).forEach(p -> {
                        final String className = FileUtils.fileNameWithoutExtension(p.getFileName().toString());
                        try {
                            applicationContext.register(shell.getClassLoader().loadClass(className, true, true, true));
                        } catch (Exception x) {
                            log(WARNING, "Unable to load {0}", x, className);
                        }
                    });
                } catch (Exception x) {
                    log(WARNING, "Unable to stream {0}", x, path);
                }
                registerBean(LoggingPostProcessor.class);
                registerBean(ProfileMBeanServerFactoryBean.class, this);
                applicationContext.refresh();
                applicationContext.start();
            }).get();
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public void start() {
        start(() -> {});
    }

    public void stop() {
        try {
            executor.submit(() -> {
                if (applicationContext != null) {
                    profileManager.getConnectionManager().unregisterConnection(getName());
                    applicationContext.close();
                }
            }).get();
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public boolean isStarted() {
        try {
            return executor.submit(() -> applicationContext != null).get();
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public AnnotationConfigApplicationContext getApplicationContext() {
        try {
            return executor.submit(() -> applicationContext).get();
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public <T> T contextResult(SafeFunction<AnnotationConfigApplicationContext, T> function) {
        try {
            return executor.submit(() -> function.apply(applicationContext)).get();
        } catch (ExecutionException x) {
            throw new IllegalStateException(x.getCause());
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    private <T> void registerBean(Class<T> klass, Object... args) {
        final ConstructorArgumentValues constructorArgumentValues = new ConstructorArgumentValues();
        for (final Object arg : args) {
            constructorArgumentValues.addGenericArgumentValue(arg);
        }
        final MutablePropertyValues pvs = new MutablePropertyValues();
        final RootBeanDefinition rootBeanDefinition = new RootBeanDefinition(klass, constructorArgumentValues, pvs);
        applicationContext.registerBeanDefinition(klass.getSimpleName(), rootBeanDefinition);
    }

    public MBeanServerConnection getConnection() {
        return contextResult(c -> c != null ? c.getBean(MBeanServerConnection.class) : null);
    }
}
