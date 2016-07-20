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

package org.marid.dependant.project.config;

import javafx.scene.control.CheckBox;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.marid.jfx.panes.GenericGridPane;
import org.marid.misc.Builder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Qualifier("projectConf")
@Order(1)
public class CommonTab extends GenericGridPane {

    public CommonTab(Model model) {
        setId("Common");
        final Organization organization = Builder.getFrom(model::getOrganization, Organization::new, model::setOrganization);
        addTextField("Name", model::getName, model::setName);
        addTextField("GroupId", model::getGroupId, model::setGroupId);
        addTextField("ArtifactId", model::getArtifactId, model::setArtifactId);
        addTextField("Version", model::getVersion, model::setVersion);
        addTextField("Description", model::getDescription, model::setDescription);
        addTextField("URL", model::getUrl, model::setUrl);
        addTextField("Inception year", model::getInceptionYear, model::setInceptionYear);
        addTextField("Organization name", organization::getName, organization::setName);
        addTextField("Organization URL", organization::getUrl, organization::setUrl);
        addSeparator();
        addControl("UI", () -> {
            final CheckBox checkBox = new CheckBox();
            checkBox.setSelected(model.getDependencies().stream().anyMatch(CommonTab::isHmi));
            checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                final String artifactId = newValue ? "marid-hmi" : "marid-runtime";
                model.getDependencies().removeIf(d -> isHmi(d) || isRuntime(d));
                final Dependency dependency = new Dependency();
                dependency.setGroupId("org.marid");
                dependency.setArtifactId(artifactId);
                dependency.setVersion("${marid.runtime.version}");
                model.getDependencies().add(dependency);
            });
            return checkBox;
        });
    }

    private static boolean isRuntime(Dependency dependency) {
        return "org.marid".equals(dependency.getGroupId()) && "marid-runtime".equals(dependency.getArtifactId());
    }

    private static boolean isHmi(Dependency dependency) {
        return "org.marid".equals(dependency.getGroupId()) && "marid-hmi".equals(dependency.getArtifactId());
    }
}
