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

package org.marid.site.context;

import org.marid.service.shutdown.ShutdownService;
import org.marid.site.SiteServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
public class SiteContext {

    @Autowired
    private Environment environment;

    @Bean
    public SiteServer siteServer() throws Exception {
        return new SiteServer(environment);
    }

    @Bean
    public ShutdownService shutdownService() throws Exception {
        return new ShutdownService("shutdownService", environment);
    }

    @PostConstruct
    public void writeShutdownPort() throws Exception {
        final File file = new File("maridSite.id");
        file.deleteOnExit();
        final String port = Integer.toString(shutdownService().getShutdownPort());
        final String selector = shutdownService().getSelector();
        Files.write(file.toPath(), Arrays.asList(port, selector));
    }
}
