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

package org.marid.ide.panes.filebrowser;

import com.google.common.collect.ImmutableMap;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.BeanEditorConfiguration;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.l10n.L10n;
import org.marid.spring.xml.MaridBeanUtils;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;
import static org.marid.jfx.icons.FontIcon.D_FOLDER;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.spring.xml.MaridBeanUtils.isFile;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileBrowserActions {

    private final ObjectProvider<BeanFileBrowserTree> tree;
    private final ObservableValue<ProjectProfile> projectProfileObservableValue;
    private final IdeDependants dependants;
    private final ObjectProvider<TabPane> ideTabPane;

    @Autowired
    public BeanFileBrowserActions(ObjectProvider<BeanFileBrowserTree> tree,
                                  ProjectManager manager,
                                  IdeDependants dependants,
                                  ObjectProvider<TabPane> ideTabPane) {
        this.tree = tree;
        this.projectProfileObservableValue = manager.profileProperty();
        this.dependants = dependants;
        this.ideTabPane = ideTabPane;
    }

    public ProjectProfile getProfile() {
        return projectProfileObservableValue.getValue();
    }

    public void onFileAdd(ActionEvent event) {
        final TextInputDialog dialog = new TextInputDialog("file");
        dialog.setTitle(L10n.s("New file"));
        dialog.setHeaderText(L10n.s("Enter file name") + ":");
        final Optional<String> value = dialog.showAndWait();
        if (value.isPresent()) {
            final String name = value.get().endsWith(".xml") ? value.get() : value.get() + ".xml";
            final TreeItem<Path> item = tree.getObject().getSelectionModel().getSelectedItem();
            final Path path = item.getValue().resolve(name);
            getProfile().getBeanFiles().put(path, new BeanFile());
        }
    }

    public BooleanBinding fileAddDisabled() {
        return Bindings.createBooleanBinding(() -> {
            final TreeItem<Path> item = tree.getObject().getSelectionModel().getSelectedItem();
            if (item == null) {
                return true;
            }
            if (item.getValue().getFileName().toString().endsWith(".xml")) {
                return true;
            }
            return false;
        }, tree.getObject().getSelectionModel().selectedItemProperty());
    }

    public void onDirAdd(ActionEvent event) {
        final TextInputDialog dialog = new TextInputDialog("directory");
        dialog.setTitle(L10n.s("New directory"));
        dialog.setHeaderText(L10n.s("Enter directory name") + ":");
        final Optional<String> value = dialog.showAndWait();
        if (value.isPresent()) {
            if (value.get().endsWith(".xml")) {
                final Alert alert = new Alert(Alert.AlertType.ERROR, L10n.m("Directory ends with .xml"), ButtonType.CLOSE);
                alert.setHeaderText(L10n.m("Directory creation error"));
                alert.showAndWait();
            } else {
                final TreeItem<Path> item = tree.getObject().getSelectionModel().getSelectedItem();
                final Path path = item.getValue().resolve(value.get());
                final TreeItem<Path> newItem = new TreeItem<>(path, glyphIcon(D_FOLDER, 16));
                item.getChildren().add(newItem);
                item.setExpanded(true);
            }
        }
    }

    public void onRename(ActionEvent event) {
        final TreeItem<Path> item = tree.getObject().getSelectionModel().getSelectedItem();
        final Path path = item.getValue();
        final boolean file = isFile(path);
        final String fileName = path.getFileName().toString();
        final String defaultValue = file ? fileName.substring(0, fileName.length() - 4) : fileName;
        final TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(file ? L10n.s("Rename file") : L10n.s("Rename directory"));
        dialog.setHeaderText(file ? L10n.s("Enter a new file name") : L10n.s("Enter a new file name"));
        final Optional<String> value = dialog.showAndWait();
        if (value.isPresent()) {
            if (file) {
                final Path newPath = path.getParent().resolve(value.get().endsWith(".xml") ? value.get() : value.get() + ".xml");
                final BeanFile beanFile = getProfile().getBeanFiles().remove(path);
                getProfile().getBeanFiles().put(newPath, beanFile);
            } else {
                if (value.get().endsWith(".xml")) {
                    final Alert alert = new Alert(Alert.AlertType.ERROR, L10n.m("Directory ends with .xml"), ButtonType.CLOSE);
                    alert.setHeaderText(L10n.m("Directory creation error"));
                    alert.showAndWait();
                } else {
                    final Map<Path, BeanFile> relativeMap = getProfile().getBeanFiles().entrySet()
                            .stream()
                            .filter(e -> e.getKey().startsWith(path))
                            .collect(toMap(e -> path.relativize(e.getKey()), Map.Entry::getValue));
                    getProfile().getBeanFiles().keySet().removeIf(p -> p.startsWith(path));
                    final Path newPath = path.getParent().resolve(value.get());
                    relativeMap.forEach((p, beanFile) -> getProfile().getBeanFiles().put(newPath.resolve(p), beanFile));
                }
            }
        }
    }

    public void onDelete(ActionEvent event) {
        final TreeItem<Path> item = tree.getObject().getSelectionModel().getSelectedItem();
        final Path path = item.getValue();
        if (isFile(path)) {
            getProfile().getBeanFiles().remove(path);
        } else {
            getProfile().getBeanFiles().keySet().forEach(p -> {
                if (p.startsWith(path)) {
                    getProfile().getBeanFiles().remove(p);
                }
            });
        }
    }

    public BooleanBinding moveDisabled() {
        return Bindings.createBooleanBinding(() -> {
            final TreeItem<Path> item = tree.getObject().getSelectionModel().getSelectedItem();
            return item == null || item == tree.getObject().getRoot();
        }, tree.getObject().getSelectionModel().selectedItemProperty());
    }

    public BooleanBinding launchDisabled() {
        return Bindings.createBooleanBinding(() -> {
            final TreeItem<Path> item = tree.getObject().getSelectionModel().getSelectedItem();
            return item == null || !MaridBeanUtils.isFile(item.getValue());
        }, tree.getObject().getSelectionModel().selectedItemProperty());
    }

    public void launchBeanEditor(ActionEvent event) {
        final Path path = tree.getObject().getSelectionModel().getSelectedItem().getValue();
        final Tab tab = ideTabPane.getObject().getTabs().stream()
                .filter(t -> getProfile().equals(t.getProperties().get("profile")))
                .filter(t -> path.equals(t.getProperties().get("path")))
                .findFirst()
                .orElse(null);
        if (tab != null) {
            ideTabPane.getObject().getSelectionModel().select(tab);
        } else {
            dependants.startDependant(
                    BeanEditorConfiguration.class,
                    ImmutableMap.of("beanFilePath", path, "profile", getProfile())
            );
        }
    }
}
