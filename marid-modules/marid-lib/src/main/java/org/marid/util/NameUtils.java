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

package org.marid.util;

import javax.naming.CompositeName;
import javax.naming.CompoundName;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov
 */
public class NameUtils {

    public static Name compositeName(String name) {
        try {
            return new CompositeName(name);
        } catch (InvalidNameException x) {
            throw new IllegalArgumentException(name, x);
        }
    }

    public static Name compoundName(String name, Properties syntax) {
        try {
            return new CompoundName(name, syntax);
        } catch (InvalidNameException x) {
            throw new IllegalArgumentException(name, x);
        }
    }

    public static Name compoundName(String name) {
        return compoundName(name, new Properties());
    }
}
