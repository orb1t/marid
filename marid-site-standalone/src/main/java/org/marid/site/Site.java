/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.marid.site;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.JavaUtilLog;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class Site {

    public static void main(String... args) throws Exception {
        System.setProperty(JavaUtilLog.class.getPackage().getName() + ".class", JavaUtilLog.class.getName());
        int port = Integer.parseInt(get("MARID.SITE.PORT", "8080"));
        String webApp = Site.class.getResource("/marid-site.war").toString();
        Server server = new Server(port);
        WebAppContext webAppContext = new WebAppContext(webApp, "/");
        server.setHandler(webAppContext);
        server.start();
        server.join();
    }

    private static String get(String key, String def) {
        String value = System.getProperty(key);
        if (value != null) {
            return value;
        } else {
            value = System.getenv(key);
            return value == null ? def : value;
        }
    }
}
