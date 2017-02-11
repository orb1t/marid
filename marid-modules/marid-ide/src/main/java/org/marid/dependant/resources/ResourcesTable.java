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
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import org.marid.ide.common.SpecialActions;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.control.CommonTableView;
import org.marid.jfx.action.FxAction;
import org.marid.logging.LogSupport;
import org.marid.spring.annotation.OrderedInit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Comparator;
import java.util.Map;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ResourcesTable extends CommonTableView<Path> implements LogSupport {

    private final ResourcesTracker resourcesTracker;

    @Autowired
    public ResourcesTable(ResourcesTracker resourcesTracker) {
        super(resourcesTracker.resources);
        this.resourcesTracker = resourcesTracker;
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setEditable(false);
    }

    @Override
    public boolean removeEnabled(ObservableList<Path> items) {
        return !items.contains(resourcesTracker.resolve("logging.properties"));
    }

    @OrderedInit(1)
    public void initPath(ProjectProfile profile) {
        final TableColumn<Path, String> column = new TableColumn<>();
        column.textProperty().bind(ls("Path"));
        column.setMinWidth(300);
        column.setPrefWidth(350);
        column.setMaxWidth(2000);
        column.setSortType(TableColumn.SortType.ASCENDING);
        column.setComparator(Comparator.comparing(Paths::get));
        column.setCellValueFactory(param -> {
            final Path base = profile.getSrcMainResources();
            final Path relative = base.relativize(param.getValue());
            return new SimpleStringProperty(relative.toString());
        });
        getColumns().add(column);
    }

    @OrderedInit(2)
    public void initSize() {
        final TableColumn<Path, String> column = new TableColumn<>();
        column.textProperty().bind(ls("Size"));
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

    @Autowired
    private void initRowFactory(SpecialActions specialActions, @Qualifier("resources") Map<String, FxAction> actionMap) {
        setRowFactory(param -> {
            final TableRow<Path> row = new TableRow<>();
            row.disableProperty().bind(row.itemProperty().isNull());
            row.setContextMenu(specialActions.contextMenu(() -> actionMap));
            return row;
        });
    }
}
