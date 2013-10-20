/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.service.silly;

import org.junit.Test;
import org.marid.Marid;
import org.marid.service.MaridService;
import org.marid.service.MaridServiceProvider;
import org.marid.service.MaridServices;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.marid.test.TestUtils.callWithClassLoader;

/**
 * @author Dmitry Ovchinnikov
 */
public class SillyMaridServiceTest {

    @Test
    public void testRun() throws Exception {
        callWithClassLoader(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Marid.main();
                final MaridService sillyService = MaridServices.getServiceByType("silly");
                assertNotNull(sillyService);
                assertEquals(1, sillyService.send(1).get());
                MaridServices.stop();
                return null;
            }
        }, MaridServiceProvider.class, SillyTestServiceProvider.class);
    }
}
