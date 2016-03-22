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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.misc.Builder;
import org.marid.spring.beans.Bean;
import org.marid.spring.beans.Beans;
import org.marid.spring.beans.BeansSerializer;
import org.marid.test.NormalTests;

import java.util.Arrays;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class BeansTest {

    @Test
    public void test1() throws Exception {
        final Beans beans = new Beans();
        beans.beans = Arrays.asList(
                Builder.build(new Bean(), b -> {
                    b.name = "x1";
                    b.scope = "prototype";
                }),
                Builder.build(new Bean(), b -> {
                    b.name = "bean2";
                    b.primary = true;
                    b.dependsOn = Arrays.asList("a", "b");
                })
        );
        BeansSerializer.serialize(beans, System.out);
    }
}
