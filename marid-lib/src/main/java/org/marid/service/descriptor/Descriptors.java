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

package org.marid.service.descriptor;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class Descriptors {

    public static <T extends Descriptor> T loadDescriptor(Class<T> cl,
                                                          Map<String, Object> bindings,
                                                          URL url) throws IOException {
        URLConnection connection = url.openConnection();
        String contentType = connection.getContentType();
        if (contentType == null) {
            contentType = URLConnection.guessContentTypeFromName(url.toString());
        } else {
            int semicolonIndex = contentType.indexOf(';');
            if (semicolonIndex > 0) {
                contentType = contentType.substring(0, semicolonIndex);
            }
            contentType = contentType.trim();
        }
        return getDescriptor(cl, bindings, contentType, url);
    }

    public static <T extends Descriptor> T loadDescriptor(Class<T> cl,
                                                          Map<String, Object> bindings,
                                                          File f) throws IOException {
        String contentType = URLConnection.guessContentTypeFromName(f.toString());
        return getDescriptor(cl, bindings, contentType, f.toURI().toURL());
    }

    private static <T extends Descriptor> T getDescriptor(Class<T> cl,
                                                          Map<String, Object> bindings,
                                                          String mime,
                                                          URL url) throws IOException {
        if (mime == null) {
            throw new IllegalArgumentException("No mime types found: " + url);
        }
        switch (mime) {
            case "text/xml":
            case "application/xml":
                return XmlDescriptor.load(cl, bindings, url);
            default:
                throw new UnsupportedOperationException(mime + " is unsupported");
        }
    }
}
