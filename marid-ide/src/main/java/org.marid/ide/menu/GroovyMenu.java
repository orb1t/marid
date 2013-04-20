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

package org.marid.ide.menu;

import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.marid.util.CollectUtils;

import java.awt.event.ActionEvent;
import java.util.*;
import java.util.logging.Logger;

import static java.util.Map.*;
import static org.marid.groovy.MaridGroovyMethods.*;
import static org.marid.ide.menu.MenuType.*;

public class GroovyMenu extends GroovyObjectSupport implements MaridMenu {

    private static final Logger log = Logger.getLogger(GroovyMenu.class.getName());

    @Override
    @SuppressWarnings("unchecked")
    public List<MenuEntry> getMenuEntries() {
        LinkedList<MenuEntry> entries = new LinkedList<>();
        Map<String, Object> map = (Map<String, Object>)getProperty("items");
        if (map == null) {
            map = Collections.emptyMap();
        }
        for (Entry<String, Object> e : map.entrySet()) {
            fillEntries(null, e, entries);
        }
        return entries;
    }

    @SuppressWarnings("unchecked")
    void fillEntries(final MenuEntry pr, final Entry<String, Object> e, final List<MenuEntry> ens) {
        final String name = e.getKey();
        final Map<String, Object> map = e.getValue() == null ?
                Collections.<String, Object>emptyMap() :
                (Map<String, Object>)e.getValue();
        Object path = map.get("path");
        final String[] p;
        if (path instanceof Object[]) {
            p = new String[((Object[])path).length];
            for (int i = 0; i < ((Object[])path).length; i++) {
                p[i] = String.valueOf(((Object[])path)[i]);
            }
        } else if (path instanceof Iterable) {
            ArrayList<String> ap = new ArrayList<>();
            for (Object o : (Iterable)path) {
                ap.add(String.valueOf(o));
            }
            p = ap.toArray(new String[ap.size()]);
        } else {
            p = pr != null ? CollectUtils.concat(pr.getPath(), pr.getName()) : new String[0];
        }
        MenuEntry me =  new MenuEntry() {
            @Override
            public String[] getPath() {
                return p;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public String getGroup() {
                return String.valueOf(map.get("group"));
            }

            @Override
            public String getLabel() {
                return isMutableLabel() ?
                        String.valueOf(map.get("label")) :
                        String.valueOf(DefaultGroovyMethods.get(map, "label",
                                StringGroovyMethods.capitalize(name).replace('_', ' ')));
            }

            @Override
            public String getCommand() {
                return String.valueOf(DefaultGroovyMethods.get(map, "command", name));
            }

            @Override
            public String getShortcut() {
                return String.valueOf(map.get("shortcut"));
            }

            @Override
            public String getDescription() {
                Object d = map.get("description");
                return isMutableDescription() ? ((Closure<String>)d).call() : String.valueOf(d);
            }

            @Override
            public String getInfo() {
                Object i = map.get("info");
                return isMutableInfo() ? ((Closure<String>)i).call() : String.valueOf(i);
            }

            @Override
            public String getIcon() {
                Object i = map.get("icon");
                return isMutableIcon() ? ((Closure<String>)i).call() : String.valueOf(i);
            }

            @Override
            public MenuType getType() {
                return !isLeaf() ? MENU : MenuType.valueOf(DefaultGroovyMethods.get(
                        map, "type", MenuType.ITEM.name()).toString().toUpperCase());
            }

            @Override
            public int getPriority() {
                return Integer.parseInt(DefaultGroovyMethods.get(map, "priority", "-1").toString());
            }

            @Override
            public boolean isMutableIcon() {
                return map.get("icon") instanceof Closure;
            }

            @Override
            public boolean isMutableLabel() {
                return map.get("label") instanceof Closure;
            }

            @Override
            public boolean isMutableInfo() {
                return map.get("info") instanceof Closure;
            }

            @Override
            public boolean isMutableDescription() {
                return map.get("description") instanceof Closure;
            }

            @Override
            public boolean hasSelectedPredicate() {
                return map.get("selected") instanceof Closure;
            }

            @Override
            public boolean hasEnabledPredicate() {
                return map.get("enabled") instanceof Closure;
            }

            @Override
            public Boolean isSelected() {
                return hasSelectedPredicate() ?
                        ((Closure<Boolean>)map.get("selected")).call() : null;
            }

            @Override
            public boolean isEnabled() {
                return hasEnabledPredicate() ?
                        ((Closure<Boolean>)map.get("enabled")).call() : true;
            }

            @Override
            public boolean isLeaf() {
                for (MenuEntry e : ens) {
                    if (e.getPath().length == getPath().length + 1 &&
                            Arrays.equals(e.getPath(), CollectUtils.concat(getPath(), name))) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void call(ActionEvent event) {
                Closure v = (Closure)map.get("action");
                if (v != null) {
                    try {
                        v.call(event);
                    } catch (Exception x) {
                        warning(log, "{0} calling error", x, this);
                    }
                }
            }

            @Override
            public String toString() {
                return DefaultGroovyMethods.join(
                        DefaultGroovyMethods.plus(getPath(), getName()), "/");
            }
        };
        ens.add(me);
        Map<String, Object> v = (Map<String, Object>)DefaultGroovyMethods.get(
                map, "items", Collections.emptyMap());
        if (v != null) {
            try {
                for (Entry<String, Object> en : v.entrySet()) {
                    fillEntries(me, en, ens);
                }
            } catch (Exception x) {
                warning(log, "Adding elements error to {0}", x, me);
            }
        }
    }
}
