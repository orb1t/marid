/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.groovy;

import groovy.lang.*;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.marid.dyn.Casting;
import org.marid.functions.SafeBiConsumer;
import org.marid.functions.SafeConsumer;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Consumer;

import static java.lang.Thread.currentThread;

/**
 * @author Dmitry Ovchinnikov
 */
public class GroovyRuntime {

    public static final CompilerConfiguration COMPILER_CONFIGURATION = newCompilerConfiguration(c -> {
    });

    public static final GroovyShell SHELL = newShell(COMPILER_CONFIGURATION, (l, s) -> {
    });
    public static final GroovyClassLoader CLASS_LOADER = SHELL.getClassLoader();

    public static Closure getClosure(GroovyCodeSource source) throws IOException {
        return (Closure) SHELL.parse(source).run();
    }

    public static CompilerConfiguration newCompilerConfiguration(Consumer<CompilerConfiguration> configurer) {
        try {
            final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
            compilerConfiguration.setSourceEncoding("UTF-8");
            compilerConfiguration.setTargetBytecode(CompilerConfiguration.JDK8);
            compilerConfiguration.setScriptBaseClass(MaridScript.class.getName());
            for (final CompilerCustomizer customizer : ServiceLoader.load(CompilerCustomizer.class)) {
                try {
                    customizer.customize(compilerConfiguration);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
            configurer.accept(compilerConfiguration);
            return compilerConfiguration;
        } catch (Exception x) {
            x.printStackTrace();
            return CompilerConfiguration.DEFAULT;
        }
    }

    public static GroovyShell newShell(CompilerConfiguration cc, SafeBiConsumer<GroovyClassLoader, GroovyShell> configurer) {
        return newShell(Thread.currentThread().getContextClassLoader(), cc, new Binding(), configurer);
    }

    public static GroovyShell newShell(ClassLoader classLoader, CompilerConfiguration cc, Binding binding, SafeBiConsumer<GroovyClassLoader, GroovyShell> configurer) {
        final GroovyShell shell = new MaridGroovyShell(classLoader, binding, cc);
        configureClassLoader(shell.getClassLoader());
        configurer.accept(shell.getClassLoader(), shell);
        return shell;
    }

    public static GroovyShell newShell(Binding binding) {
        return newShell(CLASS_LOADER, COMPILER_CONFIGURATION, binding, (l, s) -> {
        });
    }

    public static GroovyShell newShell(SafeBiConsumer<GroovyClassLoader, GroovyShell> configurer) {
        return newShell(COMPILER_CONFIGURATION, configurer);
    }

    public static GroovyShell newShell() {
        return newShell((l, s) -> {
        });
    }

    public static GroovyClassLoader newClassLoader(CompilerConfiguration cc, SafeConsumer<GroovyClassLoader> configurer) {
        final GroovyClassLoader l = new GroovyClassLoader(currentThread().getContextClassLoader(), cc);
        configureClassLoader(l);
        configurer.accept(l);
        return l;
    }

    public static GroovyClassLoader newClassLoader(SafeConsumer<GroovyClassLoader> configurer) {
        return newClassLoader(COMPILER_CONFIGURATION, configurer);
    }

    public static <T> T newInstance(Class<T> type, GroovyCodeSource codeSource) {
        final GroovyClassLoader classLoader = newClassLoader(cl -> {});
        try {
            final Script script = (Script) classLoader.parseClass(codeSource, false).newInstance();
            final Object v = script.run();
            if (v instanceof Closure) {
                return DefaultGroovyMethods.asType((Closure) v, type);
            } else if (v instanceof Map) {
                return MapProxies.newInstance(type, (Map) v);
            } else {
                return Casting.castTo(type, v);
            }
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public static <T> T newInstance(Class<T> type, URL url) {
        try {
            return newInstance(type, new GroovyCodeSource(url));
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    private static void configureClassLoader(GroovyClassLoader loader) {
        try {
            for (final CompilerUrlProvider provider : ServiceLoader.load(CompilerUrlProvider.class)) {
                try {
                    provider.getUrls().forEach(loader::addURL);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            }
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
}
