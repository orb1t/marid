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

package org.marid.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.logging.LogSupport;
import org.marid.spring.SpringUtils;
import org.marid.test.NormalTests;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import static org.marid.groovy.GroovyRuntime.SHELL;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class ServiceConfigurationTest implements LogSupport {

    @Test
    public void testConfiguration() {
        System.getProperties().put("TestService.daemons", true);
        try (final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext()) {
            ctx.register(TestService.class);
            ctx.refresh();

            final TestService service = ctx.getBean(TestService.class);
            info("Service: {0}", service);
            Assert.assertTrue(service.threadFactory.newThread(() -> {
            }).isDaemon());
        }
    }

    @Test
    public void testGroovyClosure() {
        System.getProperties().put("TestService.threadFactory", SHELL.evaluate("{s -> {r -> new Thread(r)}}"));
        System.getProperties().put("ts.x", 100);
        try (final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TestService.class);
            context.refresh();

            final TestService service = context.getBean(TestService.class);
            Assert.assertFalse(service.threadFactory.newThread(() -> {}).isDaemon());
            Assert.assertEquals(100, service.x);
        }
    }

    @Service("ts")
    static class TestService extends AbstractMaridService {

        private final int x;

        TestService(TestServiceConfiguration configuration) {
            super(configuration);
            this.x = configuration.x(this);
        }

        TestService() {
            this(SpringUtils.parse(TestService.class, TestServiceConfiguration.class));
        }
    }

    public static interface TestServiceConfiguration extends MaridServiceConfiguration {

        default int x(TestService service) {
            return 1;
        }
    }
}
