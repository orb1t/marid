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

import org.marid.runtime.exception.MaridBeanClassLoadingException;

/**
 * @author Dmitry Ovchinnikov
 */
interface MaridRuntimeUtils {

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

    static Class<?> loadClass(ClassLoader classLoader, String beanName, String className) {
        try {
            return classLoader.loadClass(className);
        } catch (Exception x) {
            throw new MaridBeanClassLoadingException(beanName, className, x);
        }
    }
}
