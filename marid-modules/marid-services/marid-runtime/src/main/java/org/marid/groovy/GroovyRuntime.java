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

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import groovy.lang.Writable;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import static org.marid.methods.LogMethods.severe;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class GroovyRuntime {

    private static final Logger LOG = Logger.getLogger(GroovyRuntime.class.getName());
    private static final CompilerConfiguration COMPILER_CONFIGURATION = new CompilerConfiguration();

    static {
        try {
            for (CompilerCustomizer customizer : ServiceLoader.load(CompilerCustomizer.class)) {
                try {
                    customizer.customize(COMPILER_CONFIGURATION);
                } catch (Exception x) {
                    warning(LOG, "Compiler customizer {0} error", x, customizer);
                }
            }
        } catch (Exception x) {
            severe(LOG, "Unable to load compiler customizers", x);
        }
    }

    public static final GroovyClassLoader CLASS_LOADER;

    static {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = GroovyRuntime.class.getClassLoader();
        }
        CLASS_LOADER = new GroovyClassLoader(classLoader, COMPILER_CONFIGURATION);
        try {
            for (CompilerUrlProvider provider : ServiceLoader.load(CompilerUrlProvider.class)) {
                try {
                    for (URL url : provider.getUrls()) {
                        CLASS_LOADER.addURL(url);
                    }
                } catch (Exception x) {
                    warning(LOG, "Unable to import URLs from the url provider {0}", x, provider);
                }
            }
        } catch (Exception x) {
            severe(LOG, "Unable to set class loader", x);
        }
    }

    public static final GroovyShell SHELL;

    static {
        Map<String, Object> bindings = new HashMap<>();
        try {
            for (BindingProvider provider : ServiceLoader.load(BindingProvider.class)) {
                try {
                    bindings.putAll(provider.getBinding());
                } catch (Exception x) {
                    warning(LOG, "Unable to import bindings from {0}", x, provider);
                }
            }

        } catch (Exception x) {
            severe(LOG, "Unable to create groovy shell", x);
        }
        SHELL = new GroovyShell(CLASS_LOADER, new Binding(bindings), COMPILER_CONFIGURATION);
        try {
            Field loaderField = GroovyShell.class.getDeclaredField("loader");
            loaderField.setAccessible(true);
            loaderField.set(SHELL, CLASS_LOADER);
        } catch (Exception x) {
            warning(LOG, "Unable to set class loader for the groovy shell", x);
        }
    }

    private static final MethodHandle CONTEXT_MH;

    static {
        MethodHandle handle = null;
        try {
            Field field = GroovyShell.class.getDeclaredField("context");
            field.setAccessible(true);
            handle = MethodHandles.lookup().unreflectSetter(field);
        } catch (Exception x) {
            warning(LOG, "Unable to get GroovyShell's context field setter", x);
        }
        CONTEXT_MH = handle;
    }

    public static GroovyShell forkShell(Binding binding) {
        if (CONTEXT_MH != null) {
            try {
                GroovyShell shell = new GroovyShell(SHELL);
                CONTEXT_MH.invokeExact(shell, binding);
                return shell;
            } catch (Throwable x) {
                warning(LOG, "Unable to fork the groovy shell", x);
                return newShell(binding);
            }
        } else {
            return newShell(binding);
        }
    }

    public static GroovyShell newShell(Binding binding) {
        return new GroovyShell(CLASS_LOADER, binding, COMPILER_CONFIGURATION);
    }

    public static String replace(String text, Map<String, Object> bindings) {
        SimpleTemplateEngine engine = new SimpleTemplateEngine(SHELL);
        Template template;
        try {
            template = engine.createTemplate(text);
        } catch (ClassNotFoundException | IOException x) {
            throw new IllegalStateException(x);
        }
        Writable writable = template.make(bindings);
        StringWriter sw = new StringWriter();
        try {
            writable.writeTo(sw);
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
        return sw.toString();
    }
}
