/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.runtime.container;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.runtime.MaridRuntime;
import org.marid.test.ManualTests;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({ManualTests.class})
public class MaridRuntimeTest {

    private static WeldContainer container;

    @BeforeClass
    public static void init() {
        container = new Weld("testMarid")
                .disableDiscovery()
                .addPackages(true, Configuration1.class)
                .addBeanClass(MaridRuntime.class)
                .initialize();
    }

    @AfterClass
    public static void destroy() {
        container.close();
    }

    @Test
    public void testBean1() {
        final Integer bean1 = container.select(Integer.class).get();
        assertEquals(1, bean1.intValue());
    }
}
