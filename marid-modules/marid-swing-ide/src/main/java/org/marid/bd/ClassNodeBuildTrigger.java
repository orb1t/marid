/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.bd;

import org.codehaus.groovy.ast.ClassNode;
import org.marid.Marid;
import org.marid.ide.components.ProfileManager;
import org.marid.logging.LogSupport;

import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ClassNodeBuildTrigger extends BuildTrigger, LogSupport {

    List<ClassNode> getClassNodes();

    @Override
    default void afterBuild() {
        final ProfileManager profileManager = Marid.getCurrentContext().getBean(ProfileManager.class);
        getClassNodes().stream().filter(n -> n != null).forEach(n -> {
            try {
                ClassHelper.saveClassNode(profileManager.getCurrentProfile().getClassesPath(), n);
            } catch (Exception x) {
                warning("Unable to save {0}", x, n.getName());
            }
        });
    }
}
