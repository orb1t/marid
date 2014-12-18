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

import static java.util.Objects.requireNonNull;
import static org.marid.util.Utils.currentClassLoader;

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
        final Path basePath;
        if (baseUrl.toString().endsWith(".jar")) {
            final FileSystem fileSystem = FileSystems.newFileSystem(Paths.get(baseUrl.toURI()), null);
            basePath = fileSystem.getRootDirectories().iterator().next();
        } else {
            basePath = Paths.get(requireNonNull(currentClassLoader().getResource("Init.groovy")).toURI()).getParent();
        }
        path = basePath.resolve("web");
        info("Path: {0}", path);
        info("Filesystem: {0} of {1}", path.getFileSystem(), path.getFileSystem().getClass().getCanonicalName());
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
