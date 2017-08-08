/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.runtime.context;

import java.io.*;
import java.util.Properties;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Dmitry Ovchinnikov
 */
public final class MaridRuntimeObject implements MaridRuntime {

    private final Function<String, Object> beanFunc;
    private final BooleanSupplier active;
    private final ClassLoader classLoader;
    private final Properties properties;
    private final MaridPlaceholderResolver placeholderResolver;

    public MaridRuntimeObject(MaridContext context, ClassLoader classLoader, Function<String, Object> beanFunc) {
        this.beanFunc = beanFunc;
        this.active = context::isActive;
        this.classLoader = classLoader;
        this.properties = new Properties(System.getProperties());
        try (final InputStream inputStream = classLoader.getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                try (final Reader reader = new InputStreamReader(inputStream, UTF_8)) {
                    properties.load(reader);
                }
            }
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
        this.placeholderResolver = new MaridPlaceholderResolver(properties);
    }

    @Override
    public Object getBean(String name) {
        return beanFunc.apply(name);
    }

    @Override
    public boolean isActive() {
        return active.getAsBoolean();
    }

    @Override
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public String resolvePlaceholders(String value) {
        return placeholderResolver.resolvePlaceholders(value);
    }

    @Override
    public Properties getApplicationProperties() {
        return properties;
    }
}
