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

package org.marid.l10n;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
public class ChainedPropertyResourceBundle extends ResourceBundle {

    private final ArrayList<Properties> propertiesList = new ArrayList<>();

    public void load(URL url, boolean useCaches) throws IOException {
        final URLConnection connection = url.openConnection();
        connection.setUseCaches(useCaches);
        try (final Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            final Properties properties = new Properties();
            properties.load(reader);
            propertiesList.add(properties);
        }
        propertiesList.trimToSize();
    }

    @Override
    public boolean containsKey(@Nonnull String key) {
        return propertiesList.stream().anyMatch(p -> p.containsKey(key)) || parent != null && parent.containsKey(key);
    }

    @Override
    protected String handleGetObject(@Nonnull String key) {
        return propertiesList.stream()
                .map(p -> p.getProperty(key))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

    @Nonnull
    @Override
    protected Set<String> handleKeySet() {
        return propertiesList.stream().flatMap(p -> p.stringPropertyNames().stream()).collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(keySet());
    }
}
