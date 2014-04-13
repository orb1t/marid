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

package org.marid.swing.input;

import org.junit.Assert;
import org.junit.Test;
import org.marid.swing.forms.Configuration;
import org.marid.swing.forms.Input;
import org.marid.swing.forms.Tab;

/**
 * @author Dmitry Ovchinnikov
 */
@Tab(node = "tab1")
public class ConfigurationTest implements Configuration {

    @Input(tab = "tab1")
    private static final Pv<Integer, FormattedIntInputControl> p = new Pv<>(FormattedIntInputControl::new, () -> 1);

    @Test
    public void test() {
        Assert.assertSame(ConfigurationTest.class, p.caller);
    }
}