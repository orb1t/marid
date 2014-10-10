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

package org.marid.site.initializers;

import org.eclipse.rap.rwt.engine.RWTServlet;
import org.eclipse.rap.rwt.engine.RWTServletContextListener;
import org.marid.site.config.SiteConfiguration;
import org.springframework.core.Ordered;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;

import static org.eclipse.rap.rwt.application.ApplicationConfiguration.CONFIGURATION_PARAM;

/**
 * @author Dmitry Ovchinnikov
 */
public class RwtInitializer implements WebApplicationInitializer, Ordered {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        servletContext.setInitParameter(CONFIGURATION_PARAM, SiteConfiguration.class.getName());
        servletContext.addListener(new RWTServletContextListener());
        final ServletRegistration.Dynamic dispatcher = servletContext.addServlet("rwtServlet", new RWTServlet());
        dispatcher.addMapping("/index/*");
        dispatcher.addMapping("/head/*");
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
