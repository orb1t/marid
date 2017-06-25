/*
 *
 */

package org.marid.dependant.project.config;

/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
