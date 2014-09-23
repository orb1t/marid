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

import org.apache.catalina.startup.Tomcat;
import org.marid.logging.LogSupport;
import org.marid.logging.Logging;

import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov
 */
public class Site implements LogSupport {

    public static void main(String... args) throws Exception {
        Logging.init("marid-site-logging.properties");
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> Log.warning("Unhandled error in {0}", e, t));
        final int port = Integer.parseInt(get("MARID.SITE.PORT", "8080"));
        final URL warUrl = requireNonNull(Thread.currentThread().getContextClassLoader().getResource("webapps/marid-site.war"));
        Log.info("WAR url: {0}", warUrl);
        final Path warPath = Paths.get(warUrl.toURI());
        final Path basePath = warPath.getParent().getParent();
        final Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(basePath.toString());
        tomcat.addWebapp("/", warPath.toString());
        tomcat.start();
        tomcat.getServer().await();
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
