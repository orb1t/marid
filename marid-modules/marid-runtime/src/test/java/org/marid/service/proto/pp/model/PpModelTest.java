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

package org.marid.service.proto.pp.model;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.service.proto.pp.PpService;
import org.marid.service.proto.pp.PpServiceConfiguration;
import org.marid.test.MaridContextLoader;
import org.marid.test.MaridSpringTests;
import org.marid.test.NormalTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
@ContextConfiguration(classes = {PpModelTestConfiguration.class}, loader = MaridContextLoader.class)
public class PpModelTest extends MaridSpringTests {

    @Autowired
    private PpServiceConfiguration configuration;

    @Autowired
    private PpService ppService;

    @Test
    public void test() throws Exception {
        Thread.sleep(10_000L);
    }
}
