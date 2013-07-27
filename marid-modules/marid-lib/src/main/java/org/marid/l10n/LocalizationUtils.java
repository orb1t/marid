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
package org.marid.l10n;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Localization utilities.
 * 
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class LocalizationUtils {
    /**
     * Empty resource bundle instance.
     */
    public static final ResourceBundle EMPTY_BUNDLE = new EmptyResourceBundle();
    
    /**
     * UTF8 Resource bundle control instance.
     */
    public static final ResourceBundle.Control UTF8_CONTROL = new Utf8Control();

    /**
     * Empty resource bundle class.
     */
    public static class EmptyResourceBundle extends ListResourceBundle {
        @Override
        protected Object[][] getContents() {
            return new Object[0][0];
        }
    }
    
    /**
     * UTF8-enabled resource bundle control.
     */
    public static class Utf8Control extends ResourceBundle.Control {
        @Override
        public ResourceBundle newBundle(
                String baseName, 
                Locale locale, 
                String format, 
                ClassLoader loader, 
                boolean reload) throws 
                        IllegalAccessException,
                        InstantiationException,
                        IOException {
            if (!FORMAT_PROPERTIES.contains(format)) {
                return super.newBundle(
                        baseName, locale, format, loader, reload);
            } else {
                String bundleName = toBundleName(baseName, locale);
                String resourceName = toResourceName(bundleName, "properties");
                if (reload) {
                    URL url = loader.getResource(resourceName);
                    if (url != null) {
                        URLConnection conn = url.openConnection();
                        conn.setUseCaches(false);
                        try (Reader r = new InputStreamReader(
                                conn.getInputStream(), UTF_8)) {
                            return new PropertyResourceBundle(r);
                        }
                    } else {
                        return null;
                    }
                } else {
                    try (Reader r = new InputStreamReader(
                            loader.getResourceAsStream(resourceName), UTF_8)) {
                        return new PropertyResourceBundle(r);
                    }
                }
            }
        }
    }
}
