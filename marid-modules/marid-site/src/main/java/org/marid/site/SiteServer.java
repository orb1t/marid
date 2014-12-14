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

package org.marid.site;

import org.marid.service.MaridServiceConfig;
import org.marid.util.Utils;
import org.marid.web.SimpleWebServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov.
 */
public class SiteServer extends SimpleWebServer {

    private final Path path;

    @Autowired
    public SiteServer(Environment environment) throws IOException, URISyntaxException {
        super(MaridServiceConfig.config("webServer", environment));
        final URL baseUrl = getClass().getProtectionDomain().getCodeSource().getLocation();
        info("Base URL: {0}", baseUrl);
        if (baseUrl.toString().endsWith(".jar")) {
            final FileSystem fileSystem = FileSystems.newFileSystem(Paths.get(baseUrl.toURI()), null);
            path = fileSystem.getRootDirectories().iterator().next();
        } else {
            path = Paths.get(Objects.requireNonNull(Utils.currentClassLoader().getResource("Init.groovy")).toURI())
                    .getParent()
                    .resolve("web");
        }
        dirMap.put("default", path);
        defaultPages.add(0, "index.groovy");
    }

    @PreDestroy
    public void destroy() throws Exception {
        if (path.getFileSystem() != FileSystems.getDefault()) {
            path.getFileSystem().close();
        }
    }
}
