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

import javafx.beans.value.WritableObjectValue;
import javafx.scene.control.TextField;
import org.apache.maven.model.Model;
import org.apache.maven.model.Organization;
import org.controlsfx.control.Notifications;
import org.marid.jfx.panes.GenericGridPane;
import org.marid.l10n.L10n;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import static org.marid.jfx.props.Props.value;
import static org.marid.misc.Builder.getFrom;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Qualifier("projectConf")
@Order(1)
public class CommonTab extends GenericGridPane {

    public CommonTab(Model model) {
        setId("Common");
        final Organization organization = getFrom(model::getOrganization, Organization::new, model::setOrganization);
        addTextField("Name", value(model::getName, model::setName));
        addNonUnicodeTextField("GroupId", value(model::getGroupId, model::setGroupId));
        addNonUnicodeTextField("ArtifactId", value(model::getArtifactId, model::setArtifactId));
        addNonUnicodeTextField("Version", value(model::getVersion, model::setVersion));
        addTextField("Description", value(model::getDescription, model::setDescription));
        addTextField("URL", value(model::getUrl, model::setUrl));
        addTextField("Inception year", value(model::getInceptionYear, model::setInceptionYear));
        addTextField("Organization name", value(organization::getName, organization::setName));
        addTextField("Organization URL", value(organization::getUrl, organization::setUrl));
    }

    private TextField addNonUnicodeTextField(String label, WritableObjectValue<String> value) {
        final TextField textField = addTextField(label, value);
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            final byte[] bytes = newValue.getBytes(StandardCharsets.UTF_8);
            final char[] chars = newValue.toCharArray();
            if (bytes.length != chars.length) {
                Notifications.create()
                        .text(L10n.m("Label {0} has invalid characters", label))
                        .title(L10n.s("Incorrect characters"))
                        .showError();
            }
        });
        return textField;
    }
}
