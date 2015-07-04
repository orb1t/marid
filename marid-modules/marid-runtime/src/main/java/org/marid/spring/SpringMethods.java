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

package org.marid.spring;

import org.marid.Marid;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class SpringMethods {

    public static AnnotationConfigApplicationContext getMaridContext(Object object) {
        return Marid.CONTEXT;
    }

    public static AnnotationConfigApplicationContext getBeans(Object object) {
        return Marid.CONTEXT;
    }

    public static <T> T getAt(GenericApplicationContext context, Class<T> type) {
        return context.getBean(type);
    }

    public static <T> T getAt(GenericApplicationContext context, Class<T> type, String name) {
        return context.getBean(name, type);
    }

    public static Object getAt(GenericApplicationContext context, String name) {
        return context.getBean(name);
    }
}
