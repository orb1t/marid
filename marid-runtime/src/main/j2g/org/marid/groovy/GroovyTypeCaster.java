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

package org.marid.groovy;

import groovy.lang.Closure;
import groovy.lang.GString;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.marid.typecast.TypeCaster;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class GroovyTypeCaster extends TypeCaster {

    @Override
    public <T> T cast(Class<T> klass, Object value) {
        if (value instanceof Collection) {
            return DefaultGroovyMethods.asType((Collection) value, klass);
        } else if (value instanceof Map) {
            return DefaultGroovyMethods.asType((Map) value, klass);
        } else if (value instanceof Number) {
            return DefaultGroovyMethods.asType((Number) value, klass);
        } else if (value instanceof Object[]) {
            return DefaultGroovyMethods.asType((Object[]) value, klass);
        } else if (value instanceof Closure) {
            return DefaultGroovyMethods.asType((Closure) value, klass);
        } else if (value instanceof GString) {
            return StringGroovyMethods.asType((GString) value, klass);
        } else if (value instanceof String) {
            return StringGroovyMethods.asType((String) value, klass);
        } else if (value instanceof CharSequence) {
            return StringGroovyMethods.asType((CharSequence) value, klass);
        } else if (value instanceof File) {
            return ResourceGroovyMethods.asType((File) value, klass);
        } else {
            return DefaultGroovyMethods.asType(value, klass);
        }
    }
}
