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

import static java.nio.charset.StandardCharsets.UTF_8;

public class MaridConfiguration {

    public final MaridPlaceholderResolver placeholderResolver;

    public MaridConfiguration(ClassLoader classLoader, Properties systemProperties) {
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
    }
}
