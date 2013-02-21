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
class AnnotatedMenu implements MaridMenu {
    @Override
    List<MenuEntry> getMenuEntries() {
        def entries = new LinkedList<MenuEntry>();
        fillEntries(getClass(), entries);
        return entries;
    }

    private void fillEntries(Class<?> cl, List<MenuEntry> entries) {
        if (!cl.isAnnotationPresent(Menu)) {
            return;
        }
        final def meta = cl.getAnnotation(Menu);
        final def item = cl.newInstance();
        def p = meta.path();
        if (p.length == 0) {
            while (true) {
                def c = cl.declaringClass;
                if (c == null) {
                    break;
                } else {
                    final def cmeta = c.getAnnotation(Menu);
                    if (cmeta.path().length == 0 && c.declaringClass != null) {
                        p = ([c.simpleName] + p.toList()) as String[];
                    } else {
                        p = (cmeta.path().toList() + p.toList()) as String[];
                        break;
                    }
                }
            }
        }
        final def path = p;
        entries << new MenuEntry() {
            def mutableLabel;
            def mutableInfo;
            def mutableDescription;
            def mutableIcon;
            def selectedPredicatePresent;
            def enabledPredicatePresent;
            def callPresent;

            {
                def call = false;
                def stringMethodSet = new TreeSet<String>();
                def booleanMethodSet = new TreeSet<String>();
                for (def m in cl.methods) {
                    if (m.parameterTypes.length == 0) {
                        if (m.returnType == String) {
                            stringMethodSet << m.name;
                        } else if (m.returnType == boolean) {
                            booleanMethodSet << m.name;
                        }
                    }
                    if (m.parameterTypes.length == 1 && m.parameterTypes[0] == ActionEvent) {
                        if (m.name == "call") {
                            call = true;
                        }
                    }
                }
                mutableLabel = "getLabel" in stringMethodSet;
                mutableInfo = "getInfo" in stringMethodSet;
                mutableDescription = "getDescription" in stringMethodSet;
                mutableIcon = "getIcon" in stringMethodSet;
                selectedPredicatePresent = "isSelected" in booleanMethodSet;
                enabledPredicatePresent = "isEnabled" in booleanMethodSet;
                callPresent = call;
            }

            @Override
            String[] getPath() {
                return path;
            }

            @Override
            String getGroup() {
                return meta.group();
            }

            @SuppressWarnings("GrUnresolvedAccess")
            @Override
            String getLabel() {
                if (!meta.label().empty) {
                    return meta.label();
                } else {
                    try {
                        return mutableLabel ? item.label : cl.simpleName;
                    } catch (x) {
                        log.warning("{0}.getLabel()", x, cl.canonicalName);
                        return null;
                    }
                }
            }

            @Override
            String getCommand() {
                return !meta.command().empty ? meta.command() : cl.canonicalName;
            }

            @Override
            String getShortcut() {
                return meta.shortcut().empty ? null : meta.shortcut();
            }

            @SuppressWarnings("GrUnresolvedAccess")
            @Override
            String getDescription() {
                if (!meta.description().empty) {
                    return meta.description();
                } else {
                    try {
                        return mutableDescription ? item.description : null;
                    } catch (x) {
                        log.warning("{0}.getDescription()", x, cl.canonicalName);
                        return null;
                    }
                }
            }

            @SuppressWarnings("GrUnresolvedAccess")
            @Override
            String getInfo() {
                if (!meta.info().empty) {
                    return meta.info();
                } else {
                    try {
                        return mutableInfo ? item.info : null;
                    } catch (x) {
                        log.warning("{0}.getInfo()", x, cl.canonicalName);
                        return null;
                    }
                }
            }

            @SuppressWarnings("GrUnresolvedAccess")
            @Override
            String getIcon() {
                if (!meta.icon().empty) {
                    return meta.icon();
                } else {
                    try {
                        return mutableIcon ? item.icon : null;
                    } catch (x) {
                        log.warning("{0}.getIcon()", x, cl.canonicalName);
                        return null;
                    }
                }
            }

            @Override
            MenuType getType() {
                return path.length == 0 ? MenuType.MENU : meta.type();
            }

            @Override
            int getPriority() {
                return meta.priority();
            }

            @Override
            boolean isMutableIcon() {
                return mutableIcon;
            }

            @Override
            boolean isMutableLabel() {
                return mutableLabel;
            }

            @Override
            boolean isMutableInfo() {
                return mutableInfo;
            }

            @Override
            boolean isMutableDescription() {
                return mutableDescription;
            }

            @Override
            boolean hasSelectedPredicate() {
                return selectedPredicatePresent;
            }

            @Override
            boolean hasEnabledPredicate() {
                return enabledPredicatePresent;
            }

            @SuppressWarnings("GrUnresolvedAccess")
            @Override
            boolean isSelected() {
                if (selectedPredicatePresent) {
                    try {
                        return item.selected;
                    } catch (x) {
                        log.warning("{0}.isSelected()", x, cl.canonicalName);
                        return false;
                    }
                }
            }

            @SuppressWarnings("GrUnresolvedAccess")
            @Override
            boolean isEnabled() {
                if (enabledPredicatePresent) {
                    try {
                        return item.enabled;
                    } catch (x) {
                        log.warning("{0}.isEnabled()", x, cl.canonicalName);
                        return false;
                    }
                }
            }

            @Override
            boolean isInitialSelected() {
                return meta.initialSelected();
            }

            @SuppressWarnings("GrUnresolvedAccess")
            @Override
            void call(ActionEvent event) {
                if (callPresent) {
                    try {
                        item.call(event);
                    } catch (x) {
                        log.warning("{0}.call({1})", x, cl.canonicalName, event);
                    }
                }
            }
        };
        for (def embedded in cl.classes) {
            fillEntries(embedded, entries);
        }
    }
}
