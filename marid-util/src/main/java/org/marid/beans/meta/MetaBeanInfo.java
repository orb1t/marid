/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.beans.meta;

import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
public class MetaBeanInfo {

    private final String name;
    private final String type;
    private final String description;
    private final Set<String> dependsOn;

    public MetaBeanInfo(String type, String name, String description, Set<String> dependsOn) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.dependsOn = dependsOn;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Set<String> getDependsOn() {
        return dependsOn;
    }

    public String getDescription() {
        return description;
    }
}
