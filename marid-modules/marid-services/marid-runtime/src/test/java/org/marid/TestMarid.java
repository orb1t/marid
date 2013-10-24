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

package org.marid;

import org.marid.service.MaridServiceProvider;
import org.marid.test.TestUtils;
import org.marid.web.TestWebServiceProvider;

import java.util.concurrent.Callable;

/**
 * @author Dmitry Ovchinnikov
 */
public class TestMarid implements Callable<Void> {

    private final String[] args;

    public TestMarid(String... args) {
        this.args = args;
    }

    @Override
    public Void call() throws Exception {
        Marid.main(args);
        return null;
    }

    public static void main(String... args) throws Exception {
        TestUtils.callWithClassLoader(new TestMarid(args),
                MaridServiceProvider.class, TestWebServiceProvider.class);
    }
}
