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

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.MapChangeListener;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.marid.ide.project.ProjectManager;
import org.marid.l10n.L10nSupport;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
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

    final ProjectManager projectManager;
    final MapChangeListener<Path, BeanFile> filesChangeListener = change -> {
        if (change.wasAdded()) {
            add(change.getKey());
        }
        if (change.wasRemoved()) {
            remove(change.getKey());
        }
    };

    public BeanFileBrowserTree(ProjectManager projectManager) {
        super(new TreeItem<>(projectManager.getProfile().getBeansDirectory(), glyphIcon(D_FOLDER, 16)));
        setShowRoot(false);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
        this.projectManager = projectManager;
        this.projectManager.profileProperty().addListener((observable, oldValue, newValue) -> {
            oldValue.getBeanFiles().removeListener(filesChangeListener);
            newValue.getBeanFiles().addListener(filesChangeListener);
            setRoot(new TreeItem<>(newValue.getBeansDirectory(), glyphIcon(D_FOLDER, 16)));
            newValue.getBeanFiles().keySet().forEach(this::add);
        });
        projectManager.getProfile().getBeanFiles().keySet().forEach(this::add);
        getColumns().add(build(new TreeTableColumn<Path, String>(), col -> {
            col.setText(s("File"));
            col.setMinWidth(500);
            col.setPrefWidth(600);
            col.setMaxWidth(2000);
            col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getFileName().toString()));
        }));
        getColumns().add(build(new TreeTableColumn<Path, Integer>(), col -> {
            col.setText(s("Bean count"));
            col.setMinWidth(200);
            col.setPrefWidth(250);
            col.setMaxWidth(300);
            col.setStyle("-fx-alignment: baseline-right");
            col.setCellValueFactory(param ->  {
                final Map<Path, BeanFile> map = projectManager.getProfile().getBeanFiles();
                final BeanFile beanFile = map.getOrDefault(param.getValue().getValue(), new BeanFile());
                return new SimpleObjectProperty<>(beanFile.beans.size());
            });
        }));
        setTreeColumn(getColumns().get(0));
    }

    private void add(Path path) {
        final Path base = projectManager.getProfile().getBeansDirectory();
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
        final Path base = projectManager.getProfile().getBeansDirectory();
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
}