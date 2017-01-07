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

package org.marid.hmi.screen;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;

import java.beans.BeanInfo;
import java.beans.Introspector;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Category({NormalTests.class})
public class HmiScreenDescriptorTest {

    @Test
    public void testDescriptor() throws Exception {
        final BeanInfo beanInfo = Introspector.getBeanInfo(HmiScreen.class);
        System.out.println(beanInfo);
    }
}