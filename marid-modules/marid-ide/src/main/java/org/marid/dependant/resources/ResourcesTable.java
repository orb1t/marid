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

package org.marid.dependant.resources;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.TableColumn;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.table.MaridTableView;
import org.marid.l10n.L10n;
import org.marid.spring.annotation.OrderedInit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.text.NumberFormat;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ResourcesTable extends MaridTableView<Path> {

    @Autowired
    public ResourcesTable(ResourcesTracker resourcesTracker) {
        super(resourcesTracker.resources.sorted());
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setEditable(false);
    }

    @OrderedInit(1)
    public void initPath(ProjectProfile profile) {
        final TableColumn<Path, String> column = new TableColumn<>(L10n.s("Path"));
        column.setMinWidth(300);
        column.setPrefWidth(350);
        column.setMaxWidth(2000);
        column.setCellValueFactory(param -> {
            final Path base = profile.getSrcMainResources();
            final Path relative = base.relativize(param.getValue());
            return new SimpleStringProperty(relative.toString());
        });
        getColumns().add(column);
    }

    @OrderedInit(2)
    public void initSize() {
        final TableColumn<Path, String> column = new TableColumn<>(L10n.s("Size"));
        column.setMinWidth(100);
        column.setPrefWidth(150);
        column.setMaxWidth(250);
        column.setStyle("-fx-alignment: baseline-right");
        column.setCellValueFactory(param -> {
            final long size = param.getValue().toFile().length();
            final NumberFormat numberFormat = NumberFormat.getIntegerInstance();
            return new SimpleStringProperty(numberFormat.format(size));
        });
        getColumns().add(column);
    }
}
