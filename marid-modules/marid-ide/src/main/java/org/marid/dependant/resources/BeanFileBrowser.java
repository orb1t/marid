/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DefaultStringConverter;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.BeanEditorParams;
import org.marid.dependant.beantree.BeanTreeConfiguration;
import org.marid.ide.common.IdeShapes;
import org.marid.ide.common.SpecialActions;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.control.CommonTableView;
import org.marid.spring.xml.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static javafx.beans.binding.Bindings.createStringBinding;
import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileBrowser extends CommonTableView<BeanFile> {

    private final ObservableList<BeanFile> source;

    @Autowired
    public BeanFileBrowser(ProjectProfile profile) {
        super(profile.getBeanFiles().sorted(BeanFile.asc()));
        source = profile.getBeanFiles();
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        setTableMenuButtonVisible(true);
    }

    @Override
    public ObservableList<BeanFile> getSourceItems() {
        return source;
    }

    @Order(1)
    @Autowired
    public void fileColumn() {
        final TableColumn<BeanFile, String> col = new TableColumn<>();
        col.textProperty().bind(ls("File"));
        col.setPrefWidth(600);
        col.setMaxWidth(2000);
        col.setCellValueFactory(p -> createStringBinding(() -> p.getValue().getFilePath(), p.getValue().path));
        col.setCellFactory(param -> new TextFieldTableCell<BeanFile, String>(new DefaultStringConverter()) {
            @Override
            public void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    final int index = getIndex();
                    final BeanFile file = getItems().get(index);
                    setGraphic(IdeShapes.fileNode(file, 16));
                }
            }
        });
        getColumns().add(col);
    }

    @Order(2)
    @Autowired
    public void dateColumn(ProjectProfile profile) {
        final TableColumn<BeanFile, String> col = new TableColumn<>();
        col.textProperty().bind(ls("Date"));
        col.setPrefWidth(300);
        col.setMaxWidth(550);
        col.setStyle("-fx-alignment: baseline-right");
        col.setCellValueFactory(param -> {
            final Path path = param.getValue().path(profile.getBeansDirectory());
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

    @Order(3)
    @Autowired
    public void beanCountColumn(ProjectProfile profile) {
        final TableColumn<BeanFile, Integer> col = new TableColumn<>();
        col.textProperty().bind(ls("Bean count"));
        col.setPrefWidth(300);
        col.setMaxWidth(550);
        col.setStyle("-fx-alignment: baseline-right");
        col.setCellValueFactory(param -> {
            final List<String> path = param.getValue().path;
            return Bindings.createObjectBinding(() -> profile.getBeanFiles().stream()
                    .filter(e -> e.path.size() >= path.size())
                    .filter(e -> e.path.subList(0, path.size()).equals(path))
                    .mapToInt(e -> e.beans.size())
                    .sum(), profile.getBeanFiles());
        });
        getColumns().add(col);
    }

    @Autowired
    private void initRowFactory(SpecialActions specialActions, @Lazy @Qualifier("fileBrowser") Map<String, FxAction> actionMap) {
        setRowFactory(v -> {
            final TableRow<BeanFile> row = new TableRow<>();
            row.disableProperty().bind(row.itemProperty().isNull());
            row.setContextMenu(specialActions.contextMenu(() -> actionMap));
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

    @Bean
    @Qualifier("fileBrowser")
    public FxAction treeAction(IdeDependants dependants) {
        return new FxAction("tree", "tree")
                .bindText("Tree")
                .setEventHandler(event -> {
                    final BeanFile file = getSelectionModel().getSelectedItem();
                    dependants.start(BeanTreeConfiguration.class, new BeanEditorParams(file), c -> {});
                })
                .bindDisabled(Bindings.isEmpty(getSelectionModel().getSelectedItems()));
    }
}
