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

package org.marid.ide.widgets;

import java.util.*;

/**
 * @author Dmitry Ovchinnikov.
 */
@FunctionalInterface
public interface WidgetProviders {

    Comparator<Class<? extends Widget>> COMPARATOR = (a, b) -> a.getClass().getName().compareTo(b.getClass().getName());

    Collection<Class<? extends Widget>> getProviders();

    static NavigableSet<Class<? extends Widget>> widgetProviders() {
        final TreeSet<Class<? extends Widget>> widgetProviders = new TreeSet<>(COMPARATOR);
        for (final WidgetProviders wps : ServiceLoader.load(WidgetProviders.class)) {
            widgetProviders.addAll(wps.getProviders());
        }
        return widgetProviders;
    }
}
