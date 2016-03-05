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

package org.marid.ide.project.editors;

import org.apache.maven.model.Model;
import org.marid.jfx.panes.GenericGridPane;

/**
 * @author Dmitry Ovchinnikov
 */
public class CommonTab extends GenericGridPane {

    public CommonTab(Model model) {
        addTextField("Name", model, "name");
        addTextField("GroupId", model, "groupId");
        addTextField("ArtifactId", model, "artifactId");
        addTextField("Version", model, "version");
        addTextField("Description", model, "description");
        addTextField("URL", model, "url");
        addTextField("Inception year", model, "inceptionYear");
        addTextField("Organization name", model.getOrganization(), "name");
        addTextField("Organization URL", model.getOrganization(), "url");
    }
}
