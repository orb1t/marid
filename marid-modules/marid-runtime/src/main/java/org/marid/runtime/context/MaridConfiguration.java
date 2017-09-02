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
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.ServiceLoader.load;
import static java.util.logging.Level.WARNING;
import static java.util.stream.StreamSupport.stream;
import static org.marid.logging.Log.log;

class MaridConfiguration {

    final MaridPlaceholderResolver placeholderResolver;
    private final MaridContextListener[] listeners;

    MaridConfiguration(ClassLoader classLoader, Properties systemProperties) {
        final Properties properties = new Properties(systemProperties);
        try (final InputStream inputStream = classLoader.getResourceAsStream("application.properties")) {
            if (inputStream != null) {
                try (final Reader reader = new InputStreamReader(inputStream, UTF_8)) {
                    properties.load(reader);
                }
            }
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }

        placeholderResolver = new MaridPlaceholderResolver(classLoader, properties);

        try {
            final ServiceLoader<MaridContextListener> serviceLoader = load(MaridContextListener.class, classLoader);
            final Stream<MaridContextListener> listenerStream = stream(serviceLoader.spliterator(), false);
            listeners = listenerStream.sorted().toArray(MaridContextListener[]::new);
        } catch (Throwable x) {
            throw new IllegalStateException("Unable to load context listeners", x);
        }
    }

    ClassLoader getClassLoader() {
        return placeholderResolver.getClassLoader();
    }

    void fireEvent(boolean reverse, Consumer<MaridContextListener> event) {
        for (int i = listeners.length - 1; i >= 0; i--) {
            final MaridContextListener listener = listeners[reverse ? i : listeners.length - i - 1];
            try {
                event.accept(listener);
            } catch (Throwable x) {
                log(WARNING, "Error in {0}", x, listener);
            }
        }
    }
}
