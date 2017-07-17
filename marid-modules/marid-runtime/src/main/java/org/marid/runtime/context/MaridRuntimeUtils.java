/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.runtime.context;

import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * @author Dmitry Ovchinnikov
 */
interface MaridRuntimeUtils {

    static Comparator<Method> methodComparator() {
        return MaridRuntimeUtils::compare;
    }

    static int compare(Method m1, Method m2) {
        if (m1.getDeclaringClass() == m2.getDeclaringClass()) {
            return m1.getName().compareTo(m2.getName());
        } else if (m1.getDeclaringClass().isAssignableFrom(m2.getDeclaringClass())) {
            return 1;
        } else {
            return -1;
        }
    }

    static Object defaultValue(Class<?> type) {
        if (type.isPrimitive()) {
            switch (type.getName()) {
                case "int": return  0;
                case "long": return  0L;
                case "float": return  0f;
                case "double": return  0d;
                case "char": return (char) 0;
                case "boolean": return false;
                case "short": return (short) 0;
                case "byte": return (byte) 0;
            }
        }
        return null;
    }
}
