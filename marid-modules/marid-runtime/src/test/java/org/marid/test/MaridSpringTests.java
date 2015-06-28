/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.test;

import org.junit.After;
import org.junit.Before;
import org.marid.Marid;
import org.marid.logging.LogSupport;
import org.marid.spring.AnnotationBaseContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridSpringTests implements LogSupport {

    protected GenericApplicationContext context;

    @Before
    public void init0() throws Exception {
        final ContextConfiguration contextConfiguration = getClass().getAnnotation(ContextConfiguration.class);
        Marid.start(Runnable::run, context -> {
            this.context = context;
            context.register(AnnotationBaseContext.class);
            if (contextConfiguration.classes().length > 0) {
                context.register(contextConfiguration.classes());
            }
            if (contextConfiguration.locations().length > 0) {
                context.scan(contextConfiguration.locations());
            }
        });
        context.getAutowireCapableBeanFactory().autowireBean(this);
        context.getAutowireCapableBeanFactory().initializeBean(this, getClass().getSimpleName());
        log(INFO, "Initialized");
    }

    @After
    public void destroy0() throws Exception {
        try {
            context.getAutowireCapableBeanFactory().destroyBean(this);
            context.close();
        } finally {
            context = null;
            log(INFO, "Destroyed");
        }
    }
}
