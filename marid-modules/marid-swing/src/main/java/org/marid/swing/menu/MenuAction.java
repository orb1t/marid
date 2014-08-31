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

package org.marid.swing.menu;

import org.marid.l10n.L10nSupport;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov
 */
public class MenuAction implements L10nSupport {

    public final String name;
    public final String group;
    public final String[] path;
    public final Map<String, Object> properties = new HashMap<>();
    public final Action action;

    public MenuAction(String name, String group, String[] path, Action action) {
        this.name = name;
        this.group = group;
        this.path = path;
        this.action = action;
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(new Object[]{name, group, path});
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MenuAction)) {
            return false;
        } else {
            final MenuAction that = (MenuAction) obj;
            return Objects.deepEquals(
                    new Object[]{this.name, this.group, this.path},
                    new Object[]{that.name, that.group, that.path});
        }
    }
}
