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

package org.marid.site.config;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.internal.application.ApplicationImpl;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class SiteConfiguration implements ApplicationConfiguration {

    @Override
    public void configure(Application application) {
        final ApplicationImpl app = (ApplicationImpl) application;
        final ServletContext servletContext = app.getApplicationContext().getServletContext();
        final WebApplicationContext webAppContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        final EntryPointConfiguration entryPointConfiguration = webAppContext.getBean(EntryPointConfiguration.class);
        entryPointConfiguration.configure(application);
    }
}
