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

package org.marid.groovy;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.marid.dyn.TypeCaster;

import java.util.Collection;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class GroovyTypeCaster extends TypeCaster {
    @Override
    public <T> T cast(Class<T> klass, Object v) {
        if (v == null) {
            return null;
        } else if (klass.isInstance(v)) {
            return klass.cast(v);
        } else if (v instanceof Number) {
            return DefaultGroovyMethods.asType((Number) v, klass);
        } else if (v instanceof Collection) {
            return DefaultGroovyMethods.asType((Collection) v, klass);
        } else if (v instanceof Map) {
            return DefaultGroovyMethods.asType((Map) v, klass);
        } else if (v instanceof Object[]) {
            return DefaultGroovyMethods.asType((Object[]) v, klass);
        } else if (v instanceof String) {
            return StringGroovyMethods.asType((String) v, klass);
        } else if (v instanceof CharSequence) {
            return StringGroovyMethods.asType((CharSequence) v, klass);
        } else if (v instanceof Closure) {
            return DefaultGroovyMethods.asType((Closure) v, klass);
        } else {
            return DefaultGroovyMethods.asType(v, klass);
        }
    }
}
