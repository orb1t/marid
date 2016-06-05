/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.spring;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;

import javax.annotation.ManagedBean;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class BeansTest {

    @Test
    public void testNewBeans() throws Exception {
        final Weld weld = new Weld("test")
                .disableDiscovery()
                .beanClasses(C.class, X.class);
        try (final WeldContainer weldContainer = weld.initialize()) {
            final C c = weldContainer.select(C.class).get();
            System.out.println(c);
        }
    }

    @ApplicationScoped
    public static class C {

        @Inject
        public C(Instance<X> x) {
            System.out.println(System.identityHashCode(x.get()));
            System.out.println(System.identityHashCode(x.get()));
        }
    }

    @ManagedBean
    public static class X {

        private final BigDecimal number = new BigDecimal(ThreadLocalRandom.current().nextDouble());

        @Produces
        public BigDecimal number() {
            return number;
        }
    }
}

