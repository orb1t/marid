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

import org.marid.runtime.context.MaridContextListener;
import org.marid.runtime.context.MaridPlaceholderResolver;

import java.io.*;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.ServiceLoader.load;
import static java.util.stream.StreamSupport.stream;

public class BeanConfiguration {

    private final MaridPlaceholderResolver placeholderResolver;
    private final MaridContextListener[] contextListeners;

    public BeanConfiguration(ClassLoader cl, Properties ps, MaridContextListener... listeners) {
        this.placeholderResolver = new MaridPlaceholderResolver(cl, ps);
        this.contextListeners = listeners;
    }

    public BeanConfiguration(ClassLoader classLoader, Properties applicationProperties) {
        this(classLoader, applicationProperties, listeners(classLoader));
    }

    public MaridPlaceholderResolver getPlaceholderResolver() {
        return placeholderResolver;
    }

    void fireEvent(Consumer<MaridContextListener> event, Consumer<Throwable> errorConsumer) {
        final boolean reverse = errorConsumer != null;
        for (int i = contextListeners.length - 1; i >= 0; i--) {
            final MaridContextListener listener = contextListeners[reverse ? i : contextListeners.length - i - 1];
            try {
                event.accept(listener);
            } catch (Throwable x) {
                if (reverse) {
                    errorConsumer.accept(x);
                } else {
                    throw x;
                }
            }
        }
    }

    private static MaridContextListener[] listeners(ClassLoader classLoader) {
        try {
            final ServiceLoader<MaridContextListener> serviceLoader = load(MaridContextListener.class, classLoader);
            final Stream<MaridContextListener> listenerStream = stream(serviceLoader.spliterator(), false);
            return listenerStream.sorted().toArray(MaridContextListener[]::new);
        } catch (Throwable x) {
            throw new IllegalStateException("Unable to load context listeners", x);
        }
    }

    private static Properties applicationProperties(ClassLoader classLoader) {
        final Properties properties = new Properties(System.getProperties());
        try (final InputStream inputStream = classLoader.getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                try (final Reader reader = new InputStreamReader(inputStream, UTF_8)) {
                    properties.load(reader);
                }
            }
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
        return properties;
    }
}
