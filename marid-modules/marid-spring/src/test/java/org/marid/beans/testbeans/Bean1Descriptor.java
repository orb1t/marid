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

package org.marid.beans.testbeans;

import org.marid.beans.Info;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Info(editors = BigInteger.class, description = "d")
public class Bean1Descriptor extends Bean1 implements Bean1Template {

    @Info(description = "constructor 1")
    public Bean1Descriptor(@Info(title = "x") int x, Double y) {
        super(x, y);
    }

    @Info(description = "getter X")
    public String getX() {
        return null;
    }

    public void setX(String x) {
    }

    public BigDecimal getY() {
        return null;
    }

    public void update(Integer z, @Info(editors = Long.class) Long h) {
    }
}
