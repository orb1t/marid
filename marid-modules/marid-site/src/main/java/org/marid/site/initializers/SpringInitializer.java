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

import org.marid.site.config.SiteConfiguration;
import org.springframework.core.Ordered;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * @author Dmitry Ovchinnikov
 */
public class SpringInitializer implements WebApplicationInitializer, Ordered {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        final AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.scan(SiteConfiguration.class.getPackage().getName());
        servletContext.addListener(new ContextLoaderListener(context));
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
