/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.misc;

import javax.annotation.Nonnull;
import java.io.UnsupportedEncodingException;
import java.net.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author Dmitry Ovchinnikov.
 */
public interface Urls {

    @Nonnull
    static URL url(@Nonnull String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException x) {
            throw new IllegalArgumentException(url, x);
        }
    }

    @Nonnull
    static URI uri(@Nonnull String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException(uri, x);
        }
    }

    @Nonnull
    static String urlEncode(@Nonnull String text) {
        try {
            return URLEncoder.encode(text, UTF_8.name());
        } catch (UnsupportedEncodingException x) {
            throw new IllegalArgumentException(UTF_8.name(), x);
        }
    }

    @Nonnull
    static String urlDecode(@Nonnull String text) {
        try {
            return URLDecoder.decode(text, UTF_8.name());
        } catch (UnsupportedEncodingException x) {
            throw new IllegalArgumentException(UTF_8.name(), x);
        }
    }
}
