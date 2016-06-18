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

import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.MapChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.l10n.L10nSupport;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.marid.jfx.icons.FontIcon.D_FILE;
import static org.marid.jfx.icons.FontIcon.D_FOLDER;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileBrowserTree extends TreeTableView<Path> implements L10nSupport {

    final ObservableValue<ProjectProfile> projectProfileObservableValue;

    @Autowired
    public BeanFileBrowserTree(ProjectManager projectManager) {
        this(projectManager.profileProperty());
    }

    protected BeanFileBrowserTree(ObservableValue<ProjectProfile> projectProfileObservableValue) {
        super(new TreeItem<>(projectProfileObservableValue.getValue().getBeansDirectory(), glyphIcon(D_FOLDER, 16)));
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
        this.projectProfileObservableValue = projectProfileObservableValue;
        final MapChangeListener<Path, BeanFile> filesChangeListener = change -> {
            if (change.wasAdded()) {
                add(change.getKey());
            }
            if (change.wasRemoved()) {
                remove(change.getKey());
            }
        };
        projectProfileObservableValue.addListener((observable, oldValue, newValue) -> {
            oldValue.getBeanFiles().removeListener(filesChangeListener);
            newValue.getBeanFiles().addListener(filesChangeListener);
            setRoot(new TreeItem<>(newValue.getBeansDirectory(), glyphIcon(D_FOLDER, 16)));
            newValue.getBeanFiles().keySet().forEach(this::add);
        });
        projectProfileObservableValue.getValue().getBeanFiles().keySet().forEach(this::add);
        getColumns().add(build(new TreeTableColumn<Path, String>(), col -> {
            col.setText(s("File"));
            col.setPrefWidth(600);
            col.setMaxWidth(2000);
            col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getFileName().toString()));
        }));
        getColumns().add(build(new TreeTableColumn<Path, FileTime>(), col -> {
            col.setText(s("Date"));
            col.setPrefWidth(250);
            col.setMaxWidth(300);
            col.setStyle("-fx-alignment: baseline-right");
            col.setCellValueFactory(param -> {
                final Path path = param.getValue().getValue();
                try {
                    return new SimpleObjectProperty<>(Files.getLastModifiedTime(path));
                } catch (IOException x) {
                    return null;
                }
            });
        }));
        getColumns().add(build(new TreeTableColumn<Path, Integer>(), col -> {
            col.setText(s("Bean count"));
            col.setPrefWidth(250);
            col.setMaxWidth(250);
            col.setStyle("-fx-alignment: baseline-right");
            col.setCellValueFactory(param -> {
                final Path path = param.getValue().getValue();
                return new SimpleObjectProperty<>(projectProfileObservableValue.getValue().getBeanFiles().entrySet().stream()
                        .filter(e -> e.getKey().startsWith(path))
                        .mapToInt(e -> e.getValue().beans.size())
                        .sum());
            });
        }));
        setTreeColumn(getColumns().get(0));
    }

    private void add(Path path) {
        final Path base = projectProfileObservableValue.getValue().getBeansDirectory();
        if (!path.startsWith(base)) {
            return;
        }
        final Path relative = base.relativize(path);
        final AtomicReference<TreeItem<Path>> itemRef = new AtomicReference<>(getRoot());
        for (int i = 1; i <= relative.getNameCount(); i++) {
            final Path suffix = relative.subpath(0, i);
            final Path p = base.resolve(suffix);
            itemRef.set(itemRef.get().getChildren()
                    .stream()
                    .filter(e -> e.getValue().equals(p))
                    .findAny()
                    .orElseGet(() -> {
                        final TreeItem<Path> newItem = new TreeItem<>(p, glyphIcon(D_FILE, 16));
                        itemRef.get().getChildren().add(newItem);
                        itemRef.get().getChildren().sort(Comparator.comparing(TreeItem::getValue));
                        itemRef.get().setExpanded(true);
                        return newItem;
                    }));
        }
    }

    private void remove(Path path) {
        final Path base = projectProfileObservableValue.getValue().getBeansDirectory();
        if (!path.startsWith(base)) {
            return;
        }
        final Path relative = base.relativize(path);
        final AtomicReference<TreeItem<Path>> itemRef = new AtomicReference<>(getRoot());
        for (int i = 1; i <= relative.getNameCount(); i++) {
            final Path suffix = relative.subpath(0, i);
            final Path p = base.resolve(suffix);
            itemRef.set(itemRef.get().getChildren()
                    .stream()
                    .filter(e -> e.getValue().equals(p))
                    .findAny()
                    .orElse(null));
            if (itemRef.get() == null) {
                break;
            }
        }
        if (itemRef.get() != null) {
            itemRef.get().getParent().getChildren().remove(itemRef.get());
        }
    }

    public void onFileAddEventHandler(ActionEvent event) {
        final TextInputDialog dialog = new TextInputDialog("file");
        dialog.setTitle(s("New file"));
        dialog.setHeaderText(s("Enter file name") + ":");
        final Optional<String> value = dialog.showAndWait();
        if (value.isPresent()) {
            final String name = value.get().endsWith(".xml") ? value.get() : value.get() + ".xml";
            final TreeItem<Path> item = getSelectionModel().getSelectedItem();
            final Path path = item.getValue().resolve(name);
            projectProfileObservableValue.getValue().getBeanFiles().put(path, new BeanFile());
            final TreeItem<Path> newItem = new TreeItem<>(path, glyphIcon(D_FILE));
            item.getChildren().add(newItem);
            item.setExpanded(true);
        }
    }

    public BooleanBinding fileAddDisabled() {
        return Bindings.createBooleanBinding(() -> {
            final TreeItem<Path> item = getSelectionModel().getSelectedItem();
            if (item == null) {
                return true;
            }
            if (item.getValue().getFileName().toString().endsWith(".xml")) {
                return true;
            }
            return false;
        }, getSelectionModel().selectedItemProperty());
    }

    public void onDirAddEventHandler(ActionEvent event) {
        final TextInputDialog dialog = new TextInputDialog("directory");
        dialog.setTitle(s("New directory"));
        dialog.setHeaderText(s("Enter directory name") + ":");
        final Optional<String> value = dialog.showAndWait();
        if (value.isPresent()) {
            final TreeItem<Path> item = getSelectionModel().getSelectedItem();
            final Path path = item.getValue().resolve(value.get());
            final TreeItem<Path> newItem = new TreeItem<>(path, glyphIcon(D_FILE));
            item.getChildren().add(newItem);
            item.setExpanded(true);
        }
    }
}