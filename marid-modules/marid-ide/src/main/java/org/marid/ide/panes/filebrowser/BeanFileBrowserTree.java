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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import org.marid.ide.project.ProjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.marid.jfx.icons.FontIcon.D_FOLDER;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileBrowserTree extends TreeTableView<Path> {

    @Autowired
    public BeanFileBrowserTree(ProjectManager projectManager) {
        super(new TreeItem<>(projectManager.getProfile().getBeansDirectory(), glyphIcon(D_FOLDER, 16)));
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);

        getColumns().add(build(new TreeTableColumn<Path, String>(), col -> {
            col.setText(s("File"));
            col.setPrefWidth(600);
            col.setMaxWidth(2000);
            col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getFileName().toString()));
        }));
        getColumns().add(build(new TreeTableColumn<Path, String>(), col -> {
            col.setText(s("Date"));
            col.setPrefWidth(250);
            col.setMaxWidth(300);
            col.setStyle("-fx-alignment: baseline-right");
            col.setCellValueFactory(param -> {
                final Path path = param.getValue().getValue();
                try {
                    final FileTime fileTime = Files.getLastModifiedTime(path);
                    final Instant instant = fileTime.toInstant();
                    return new SimpleStringProperty(instant.atZone(ZoneId.systemDefault()).format(ISO_LOCAL_DATE_TIME));
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
                return new SimpleObjectProperty<>(projectManager.getProfile().getBeanFiles().entrySet().stream()
                        .filter(e -> e.getKey().startsWith(path))
                        .mapToInt(e -> e.getValue().beans.size())
                        .sum());
            });
        }));
        setTreeColumn(getColumns().get(0));
    }
}
