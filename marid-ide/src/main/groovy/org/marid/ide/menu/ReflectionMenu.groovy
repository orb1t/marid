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

@Log
class ReflectionMenu implements MaridMenu {

    @Override
    List<MenuEntry> getMenuEntries() {
        def entries = new LinkedList<MenuEntry>();
        fillEntries(null, getClass(), entries);
        return entries;
    }

    private void fillEntries(final MenuEntry parent, final Class<?> cl, List<MenuEntry> entries) {
        final def item = cl == getClass() ? this : cl.newInstance();
        def me =  new MenuEntry() {
            @Override
            String[] getPath() {
                def p = item.hasProperty("path") ? item["path"] as String[] : new String[0];
                return p.size() > 0 ? p : parent != null ? parent.path + parent.name : p;
            }

            @Override
            String getName() {
                return item.hasProperty("name") ? item["name"] : cl.simpleName;
            }

            @Override
            String getGroup() {
                return item.hasProperty("group") ? item["group"] : null;
            }

            @Override
            String getLabel() {
                return item.hasProperty("label") ? item["label"] as String : name;
            }

            @Override
            String getCommand() {
                return item.hasProperty("command") ? item["command"] as String : cl.canonicalName;
            }

            @Override
            String getShortcut() {
                return item.hasProperty("shortcut") ? item["shortcut"] as String : null;
            }

            @Override
            String getDescription() {
                return item.hasProperty("description") ? item["description"] as String : null;
            }

            @Override
            String getInfo() {
                return item.hasProperty("info") ? item["info"] as String : null;
            }

            @Override
            String getIcon() {
                return item.hasProperty("icon") ? item["icon"] as String : null;
            }

            @Override
            MenuType getType() {
                return path.length == 0 ? MenuType.MENU :
                    item.hasProperty("type") ? item["type"] as MenuType : MenuType.ITEM;
            }

            @Override
            int getPriority() {
                return item.hasProperty("priority") ? item["priority"] as int : -1;
            }

            @Override
            boolean isMutableIcon() {
                return cl.methods
                        .findAll{it.parameterTypes.length == 0}
                        .collect{it.name}.contains("getIcon");
            }

            @Override
            boolean isMutableLabel() {
                return cl.methods
                        .findAll{it.parameterTypes.length == 0}
                        .collect{it.name}.contains("getLabel");
            }

            @Override
            boolean isMutableInfo() {
                return cl.methods
                        .findAll{it.parameterTypes.length == 0}
                        .collect{it.name}.contains("getInfo");
            }

            @Override
            boolean isMutableDescription() {
                return cl.methods
                        .findAll{it.parameterTypes.length == 0}
                        .collect{it.name}.contains("getDescription");
            }

            @Override
            boolean hasSelectedPredicate() {
                return cl.methods
                        .findAll{it.parameterTypes.length == 0 && it.returnType == boolean}
                        .collect{it.name}.contains("isSelected");
            }

            @Override
            boolean hasEnabledPredicate() {
                return cl.methods
                        .findAll{it.parameterTypes.length == 0 && it.returnType == boolean}
                        .collect{it.name}.contains("isEnabled");
            }

            @Override
            boolean isSelected() {
                return item.hasProperty("selected") ? item["selected"] as boolean : true;
            }

            @Override
            boolean isEnabled() {
                return item.hasProperty("enabled") ? item["enabled"] as boolean : true;
            }

            @Override
            void call(ActionEvent event) {
                if (item instanceof Closure) {
                    item(event);
                } else if (cl.methods
                        .findAll{it.parameterTypes.length == 1}
                        .collect{it.name}.contains("call")) {
                    item.call(event);
                }
            }

            @Override
            String toString() {
                return (path + name).toList().join("/");
            }
        };
        entries << me;
        for (def embedded in cl.classes) {
            fillEntries(me, embedded, entries);
        }
    }
}
