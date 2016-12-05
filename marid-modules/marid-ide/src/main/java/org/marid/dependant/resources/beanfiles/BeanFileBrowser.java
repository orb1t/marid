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

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.util.Pair;
import org.marid.ide.common.SpecialActions;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.idefx.CommonTableView;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.OrderedInit;
import org.marid.spring.xml.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Comparator;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileBrowser extends CommonTableView<Pair<Path, BeanFile>> {

    private final ObservableList<Pair<Path, BeanFile>> source;

    @Autowired
    public BeanFileBrowser(ProjectProfile profile) {
        super(profile.getBeanFiles().sorted(Comparator.comparing(Pair::getKey)));
        source = profile.getBeanFiles();
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
    }

    @Override
    protected ObservableList<Pair<Path, BeanFile>> getSourceItems() {
        return source;
    }

    @OrderedInit(1)
    public void fileColumn(ProjectManager projectManager) {
        final TableColumn<Pair<Path, BeanFile>, String> col = new TableColumn<>();
        col.textProperty().bind(ls("File"));
        col.setPrefWidth(600);
        col.setMaxWidth(2000);
        col.setCellValueFactory(param -> {
            final Path path = projectManager.getProfile().getBeansDirectory().relativize(param.getValue().getKey());
            return new SimpleStringProperty(path.toString());
        });
        getColumns().add(col);
    }

    @OrderedInit(2)
    public void dateColumn() {
        final TableColumn<Pair<Path, BeanFile>, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Date"));
        col.setPrefWidth(250);
        col.setMaxWidth(300);
        col.setStyle("-fx-alignment: baseline-right");
        col.setCellValueFactory(param -> {
            final Path path = param.getValue().getKey();
            try {
                final FileTime fileTime = Files.getLastModifiedTime(path);
                final Instant instant = fileTime.toInstant();
                return new SimpleStringProperty(instant.atZone(ZoneId.systemDefault()).format(ISO_LOCAL_DATE_TIME));
            } catch (IOException x) {
                return null;
            }
        });
        getColumns().add(col);
    }

    @OrderedInit(3)
    public void beanCountColumn(ProjectManager projectManager) {
        final TableColumn<Pair<Path, BeanFile>, Integer> col = new TableColumn<>();
        col.textProperty().bind(ls("Bean count"));
        col.setPrefWidth(250);
        col.setMaxWidth(250);
        col.setStyle("-fx-alignment: baseline-right");
        col.setCellValueFactory(param -> {
            final Path path = param.getValue().getKey();
            return new SimpleObjectProperty<>(projectManager.getProfile().getBeanFiles().stream()
                    .filter(e -> e.getKey().startsWith(path))
                    .mapToInt(e -> e.getValue().beans.size())
                    .sum());
        });
        getColumns().add(col);
    }

    @Autowired
    private void initRowFactory(SpecialActions specialActions) {
        setRowFactory(v -> {
            final TableRow<Pair<Path, BeanFile>> row = new TableRow<>();
            row.disableProperty().bind(row.itemProperty().isNull());
            row.setContextMenu(specialActions.contextMenu(Collections::emptyMap));
            return row;
        });
    }

    @Autowired
    private void initEdit(BeanFileBrowserActions actions, FxAction editAction) {
        editAction.on(this, action -> {
            action.setEventHandler(actions::launchBeanEditor);
            action.bindDisabled(getSelectionModel().selectedItemProperty().isNull());
        });
    }

    @Autowired
    private void initAdd(BeanFileBrowserActions actions, FxAction addAction) {
        addAction.on(this, action -> action.setEventHandler(actions::onFileAdd));
    }

    @Autowired
    private void initRename(BeanFileBrowserActions actions, FxAction renameAction) {
        renameAction.on(this, action -> {
            action.setEventHandler(actions::onRename);
            action.bindDisabled(getSelectionModel().selectedItemProperty().isNull());
        });
    }
}
