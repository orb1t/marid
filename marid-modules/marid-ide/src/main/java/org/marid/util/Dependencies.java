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

package org.marid.util;

import org.apache.maven.model.Dependency;

import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface Dependencies {

    static boolean equals(Dependency d1, Dependency d2) {
        if (!Objects.equals(d1.getGroupId(), d2.getGroupId())) {
            return false;
        }
        if (!Objects.equals(d1.getArtifactId(), d2.getArtifactId())) {
            return false;
        }
        if (!Objects.equals(d1.getVersion(), d2.getVersion())) {
            return false;
        }
        return true;
    }
}
