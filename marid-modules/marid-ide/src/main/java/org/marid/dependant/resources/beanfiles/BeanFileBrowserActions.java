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

package org.marid.dependant.resources.beanfiles;

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextInputDialog;
import javafx.util.Pair;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.BeanEditorConfiguration;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.BeanFile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Optional;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileBrowserActions {

    private final ObjectProvider<BeanFileBrowser> browser;
    private final ObservableValue<ProjectProfile> projectProfileObservableValue;
    private final IdeDependants dependants;
    private final ObjectProvider<TabPane> ideTabPane;

    @Autowired
    public BeanFileBrowserActions(ObjectProvider<BeanFileBrowser> browser,
                                  ProjectManager manager,
                                  IdeDependants dependants,
                                  ObjectProvider<TabPane> ideTabPane) {
        this.browser = browser;
        this.projectProfileObservableValue = manager.profileProperty();
        this.dependants = dependants;
        this.ideTabPane = ideTabPane;
    }

    public ProjectProfile getProfile() {
        return projectProfileObservableValue.getValue();
    }

    public void onFileAdd(ActionEvent event) {
        final TextInputDialog dialog = new TextInputDialog("file");
        dialog.setTitle(s("New file"));
        dialog.setHeaderText(s("Enter file name") + ":");
        final Optional<String> value = dialog.showAndWait();
        if (value.isPresent()) {
            final String name = value.get().endsWith(".xml") ? value.get() : value.get() + ".xml";
            final Path path = getProfile().getBeansDirectory().resolve(name);
            getProfile().getBeanFiles().add(new Pair<>(path, new BeanFile()));
        }
    }

    public void onRename(ActionEvent event) {
        final Path path = browser.getObject().getSelectionModel().getSelectedItem().getKey();
        final String fileName = path.getFileName().toString();
        final String defaultValue = fileName.substring(0, fileName.length() - 4);
        final TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(s("Rename file"));
        dialog.setHeaderText(s("Enter a new file name"));
        final Optional<String> value = dialog.showAndWait();
        if (value.isPresent()) {
            final Path newPath = path.getParent().resolve(value.get().endsWith(".xml") ? value.get() : value.get() + ".xml");
            getProfile().getBeanFiles().filtered(p -> p.getKey().equals(path)).replaceAll(p -> new Pair<>(newPath, p.getValue()));
        }
    }

    public void onDelete(ActionEvent event) {
        getProfile().getBeanFiles().removeAll(browser.getObject().getSelectionModel().getSelectedItems());
    }

    public void launchBeanEditor(ActionEvent event) {
        final Path path = browser.getObject().getSelectionModel().getSelectedItem().getKey();
        dependants.start("beanEditor", BeanEditorConfiguration.class, c -> {
            c.profile = getProfile();
            c.beanFilePath = path;
        });
    }
}
