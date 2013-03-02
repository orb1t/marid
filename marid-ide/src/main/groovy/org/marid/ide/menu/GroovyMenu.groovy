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

package org.marid.ide.menu

import groovy.util.logging.Log

import java.awt.event.ActionEvent

import static java.util.Map.Entry
import static org.marid.ide.menu.MenuType.*

@SuppressWarnings("GroovyAssignabilityCheck")
@Log
class GroovyMenu extends GroovyObjectSupport implements MaridMenu {

    @Override
    List<MenuEntry> getMenuEntries() {
        def entries = new LinkedList<MenuEntry>();
        if (hasProperty("items") && getProperty("items") instanceof Map) {
            ((Map)getProperty("items")).each {en ->
                fillEntries(null, en, entries);
            }
        }
        return entries;
    }

    void fillEntries(final MenuEntry parent, final Entry e, final List<MenuEntry> entries) {
        final def name = e.key as String;
        final def map = e.value instanceof Map ? (Map)e.value : Collections.EMPTY_MAP;
        def me =  new MenuEntry() {
            @Override
            String[] getPath() {
                def p = map.containsKey("path") ? map["path"] as String[] : new String[0];
                return p.size() > 0 ? p : parent != null ? parent.path + parent.name : p;
            }

            @Override
            String getName() {
                return name;
            }

            @Override
            String getGroup() {
                return map["group"] as String;
            }

            @Override
            String getLabel() {
                return isMutableLabel() ? map["label"]() as String :
                    map.get("label", name.capitalize());
            }

            @Override
            String getCommand() {
                return map.get("command", name) as String;
            }

            @Override
            String getShortcut() {
                return map["shortcut"] as String;
            }

            @Override
            String getDescription() {
                def d = map["description"];
                return isMutableDescription() ? d() as String : d as String;
            }

            @Override
            String getInfo() {
                return isMutableInfo() ? map["info"]() as String : map["info"] as String;
            }

            @Override
            String getIcon() {
                return isMutableIcon() ? map["icon"]() as String : map["icon"] as String;
            }

            @Override
            MenuType getType() {
                return !isLeaf() ? MENU : map.get("type", ITEM) as MenuType;
            }

            @Override
            int getPriority() {
                return map.get("priority", -1) as int;
            }

            @Override
            boolean isMutableIcon() {
                return map["icon"] instanceof Closure;
            }

            @Override
            boolean isMutableLabel() {
                return map["label"] instanceof Closure;
            }

            @Override
            boolean isMutableInfo() {
                return map["info"] instanceof Closure;
            }

            @Override
            boolean isMutableDescription() {
                return map["description"] instanceof Closure;
            }

            @Override
            boolean hasSelectedPredicate() {
                return map["selected"] instanceof Closure;
            }

            @Override
            boolean hasEnabledPredicate() {
                return map["enabled"] instanceof Closure;
            }

            @Override
            Boolean isSelected() {
                return hasSelectedPredicate() ? map["selected"]() as Boolean : null;
            }

            @Override
            boolean isEnabled() {
                return hasEnabledPredicate() ? map["enabled"]() as boolean : true;
            }

            @Override
            boolean isLeaf() {
                return !entries.any {it.path.length == path.length + 1 && it.path == path + name};
            }

            @Override
            void call(ActionEvent event) {
                def v = map["action"];
                if (v != null) {
                    try {
                        v(event);
                    } catch (x) {
                        log.warning("{0} calling error", x, this);
                    }
                }
            }

            @Override
            String toString() {
                return (path + name).toList().join("/");
            }
        };
        entries << me;
        def v = map.get("items", Collections.EMPTY_MAP) as Map;
        if (v != null) {
            try {
                v.each {en ->
                    fillEntries(me, en, entries);
                }
            } catch (x) {
                log.warning("Adding elements error to {0}", x, me)
            }
        }
    }
}
