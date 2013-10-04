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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.http.fileupload.FileUtils;

/**
 * @author Dmitry Ovchinnikov
 */
public class Site {
    
    private static final Logger LOG = Logger.getLogger(Site.class.getName());

    public static void main(String... args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable e) {
                final LogRecord r = new LogRecord(Level.WARNING, "Unhandled error in {0}");
                r.setParameters(new Object[] {t});
                r.setThrown(e);
                LOG.log(r);
            }
        });
        final int port = Integer.parseInt(get("MARID.SITE.PORT", "8080"));
        final Path basePath = Paths.get("base");
        if (Files.exists(basePath)) {
            FileUtils.deleteDirectory(basePath.toFile());
        }
        final Path webApps = basePath.resolve("webapps");
        Files.createDirectories(webApps);
        try (final InputStream is = Site.class.getResourceAsStream("/marid-site.war")) {
            Files.copy(is, webApps.resolve("marid-site.war"));
        }
        final Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.setBaseDir(basePath.toString());
        tomcat.addWebapp("/", "marid-site.war");
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
