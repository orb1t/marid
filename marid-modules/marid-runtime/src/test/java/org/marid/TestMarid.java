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

package org.marid;

import org.marid.web.SimpleWebServer;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class TestMarid {

    public static void main(String... args) throws Exception {
        final SimpleWebServer webServer = new SimpleWebServer() {
            @Override
            protected Map<String, Path> dirMap() {
                final URL url = SimpleWebServer.class.getResource("site/index.html");
                try {
                    return Collections.singletonMap("default", Paths.get(url.toURI()).getParent());
                } catch (URISyntaxException x) {
                    throw new IllegalStateException(x);
                }
            }
        };
        webServer.start();
    }
}
