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

package org.marid.dpp;

import groovy.lang.Closure;
import org.marid.methods.PropMethods;
import org.marid.tree.StaticTreeObject;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import static java.util.Collections.emptyMap;
import static org.marid.methods.LogMethods.info;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class DppUtil {

    public static LinkedList<Closure> func(Iterable iterable) {
        final LinkedList<Closure> fs = new LinkedList<>();
        for (final Object o : iterable) {
            func(o, fs);
        }
        return fs;
    }

    public static void func(Object o, LinkedList<Closure> cs) {
        if (o instanceof Collection) {
            for (final Object co : (Collection) o) {
                func(co, cs);
            }
        } else if (o instanceof Closure) {
            cs.add((Closure) o);
        }
    }

    public static void addTasks(Logger l, StaticTreeObject o, Map<String, StaticTreeObject> kids, Map params) {
        final Map groups = PropMethods.get(params, Map.class, "groups", emptyMap());
        for (final Object e : groups.entrySet()) {
            final Entry entry = (Entry) e;
            final String groupName = String.valueOf(entry.getKey());
            final Map groupParams = PropMethods.get(groups, Map.class, entry.getKey(), emptyMap());
            kids.put(groupName, new DppGroup(o, groupName, groupParams));
            info(l, "Added group {0}", groupName);
        }
        final Map tasks = PropMethods.get(params, Map.class, "tasks", emptyMap());
        for (final Object e : tasks.entrySet()) {
            final Entry entry = (Entry) e;
            final String taskName = String.valueOf(entry.getKey());
            final Map taskParams = PropMethods.get(tasks, Map.class, entry.getKey(), emptyMap());
            if (kids.containsKey(taskName)) {
                warning(l, "Task name is equals to a group name {0}", taskName);
            } else {
                kids.put(taskName, new DppTask(o, taskName, taskParams));
                info(l, "Added task {0}", taskName);
            }
        }
    }
}
