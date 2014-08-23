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

package org.marid.ide.swing.context;

import groovy.lang.GroovyCodeSource;
import org.marid.groovy.GroovyRuntime;
import org.marid.logging.LogSupport;
import org.marid.swing.MaridAction;
import org.marid.swing.menu.MenuAction;
import org.marid.swing.menu.MenuActionList;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.util.Enumeration;
import java.util.List;

import static org.marid.dyn.TypeCaster.TYPE_CASTER;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class MenuContext implements LogSupport {

    @Bean
    public MenuActionList ideMenuActionList() {
        final MenuActionList menuActions = new MenuActionList();
        try {
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            for (final Enumeration<URL> e = cl.getResources("menu/Menu.groovy"); e.hasMoreElements();) {
                final URL url = e.nextElement();
                try {
                    final List list = (List) GroovyRuntime.SHELL.evaluate(new GroovyCodeSource(url));
                    for (final Object le : list) {
                        if (!(le instanceof List)) {
                            continue;
                        }
                        final List l = (List) le;
                        final String[] path;
                        final String group;
                        final String name;
                        final String icon;
                        final MaridAction.MaridActionListener mal;
                        final Object[] args;
                        switch (l.size()) {
                            case 6:
                                path = TYPE_CASTER.cast(String[].class, l.get(0));
                                group = TYPE_CASTER.cast(String.class, l.get(1));
                                name = TYPE_CASTER.cast(String.class, l.get(2));
                                icon = TYPE_CASTER.cast(String.class, l.get(3));
                                mal = TYPE_CASTER.cast(MaridAction.MaridActionListener.class, l.get(4));
                                args = TYPE_CASTER.cast(Object[].class, l.get(5));
                                break;
                            case 5:
                                path = TYPE_CASTER.cast(String[].class, l.get(0));
                                group = TYPE_CASTER.cast(String.class, l.get(1));
                                name = TYPE_CASTER.cast(String.class, l.get(2));
                                icon = TYPE_CASTER.cast(String.class, l.get(3));
                                mal = TYPE_CASTER.cast(MaridAction.MaridActionListener.class, l.get(4));
                                args = new String[0];
                                break;
                            case 3:
                                path = TYPE_CASTER.cast(String[].class, l.get(0));
                                group = TYPE_CASTER.cast(String.class, l.get(1));
                                name = TYPE_CASTER.cast(String.class, l.get(2));
                                icon = null;
                                mal = null;
                                args = null;
                                break;
                            default:
                                warning("Invalid menu line: {0}", l);
                                continue;
                        }
                        final MaridAction action = mal == null ? null : new MaridAction(s(name), icon, mal, args);
                        menuActions.add(new MenuAction(name, group, path, action));
                    }
                } catch (Exception x) {
                    warning("Unable to load menu entries from {0}", x, url);
                }
            }
        } catch (Exception x) {
            warning("Unable to load menu entries", x);
        }
        return menuActions;
    }
}
