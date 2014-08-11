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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static java.lang.Thread.currentThread;
import static org.marid.methods.LogMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class GroovyRuntime {

    private static final Logger LOG = Logger.getLogger(GroovyRuntime.class.getName());
    private static final CompilerConfiguration COMPILER_CONFIGURATION = newCompilerConfiguration(c -> {
    });

    public static final GroovyShell SHELL = newShell(COMPILER_CONFIGURATION, (l, s) -> {});
    public static final GroovyClassLoader CLASS_LOADER = SHELL.getClassLoader();

    public static GroovyShell newShell(Binding binding) {
        return new GroovyShell(CLASS_LOADER, binding, COMPILER_CONFIGURATION);
    }

    public static Closure getClosure(GroovyCodeSource source) throws IOException {
        return (Closure) SHELL.parse(source).run();
    }

    public static CompilerConfiguration newCompilerConfiguration(Consumer<CompilerConfiguration> configurer) {
        try {
            final CompilerConfiguration compilerConfiguration = new CompilerConfiguration();
            for (final CompilerCustomizer customizer : ServiceLoader.load(CompilerCustomizer.class)) {
                try {
                    customizer.customize(compilerConfiguration);
                    fine(LOG, "Compiler customizer {0} loaded", customizer);
                } catch (Exception x) {
                    warning(LOG, "Compiler customizer {0} error", x, customizer);
                }
            }
            configurer.accept(compilerConfiguration);
            return compilerConfiguration;
        } catch (Exception x) {
            severe(LOG, "Unable to load compiler customizers", x);
            return CompilerConfiguration.DEFAULT;
        }
    }

    public static GroovyShell newShell(CompilerConfiguration cc, BiConsumer<GroovyClassLoader, GroovyShell> configurer) {
        final Map<String, Object> bindings = new HashMap<>();
        try {
            for (final BindingProvider provider : ServiceLoader.load(BindingProvider.class)) {
                try {
                    bindings.putAll(provider.getBinding());
                } catch (Exception x) {
                    warning(LOG, "Unable to import bindings from {0}", x, provider);
                }
            }

        } catch (Exception x) {
            severe(LOG, "Unable to create groovy shell", x);
        }
        final GroovyShell shell = new GroovyShell(currentThread().getContextClassLoader(), new Binding(bindings), cc);
        configurer.accept(shell.getClassLoader(), shell);
        return shell;
    }

    public static GroovyClassLoader newClassLoader(CompilerConfiguration cc, Consumer<GroovyClassLoader> configurer) {
        final GroovyClassLoader l = new GroovyClassLoader(currentThread().getContextClassLoader(), cc);
        try {
            for (final CompilerUrlProvider provider : ServiceLoader.load(CompilerUrlProvider.class)) {
                try {
                    provider.getUrls().forEach(CLASS_LOADER::addURL);
                } catch (Exception x) {
                    warning(LOG, "Unable to import URLs from the url provider {0}", x, provider);
                }
            }
            configurer.accept(l);
        } catch (Exception x) {
            severe(LOG, "Unable to set class loader", x);
        }
        return l;
    }
}
