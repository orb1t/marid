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

package org.marid.ide.beaneditor;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import org.codehaus.plexus.util.FileUtils;
import org.marid.ide.beaneditor.ui.*;
import org.marid.ide.project.ProjectProfile;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

/**
 * @author Dmitry Ovchinnikov
 */
class BeanTree extends TreeTableView<Object> implements LogSupport, L10nSupport, BeanTreeConstants {

    private final BeanEditor beanEditor;

    BeanTree(ProjectProfile rootObject, BeanEditor beanEditor) {
        super(new TreeItem<>(rootObject, new ImageView(ROOT)));
        this.beanEditor = beanEditor;
        setColumnResizePolicy(UNCONSTRAINED_RESIZE_POLICY);
        getColumns().add(nameColumn());
        getColumns().add(typeColumn());
        getColumns().add(valueColumn());
        setTreeColumn(getColumns().get(0));
        setEditable(true);
        setRowFactory(param -> {
            final TreeTableRow<Object> row = new TreeTableRow<>();
            row.setPrefHeight(30);
            return row;
        });
    }

    ProjectProfile getProfile() {
        return (ProjectProfile) getRoot().getValue();
    }

    public void load() {
        try {
            beanEditor.loader.load(getRoot());
        } catch (Exception x) {
            log(WARNING, "Unable to load beans", x);
        }
    }

    public void save() {
        try {
            final ProjectProfile profile = getProfile();
            FileUtils.cleanDirectory(profile.getBeansDirectory().toFile());
            beanEditor.saver.save(getRoot());
        } catch (Exception x) {
            log(WARNING, "Unable to save beans", x);
        }
    }

    private TreeTableColumn<Object, String> nameColumn() {
        final TreeTableColumn<Object, String> column = new TreeTableColumn<>(s("Name"));
        column.setCellValueFactory(new NameFactory());
        column.setCellFactory(c -> new NameCell(c, beanEditor));
        column.setPrefWidth(400);
        return column;
    }

    private TreeTableColumn<Object, String> typeColumn() {
        final TreeTableColumn<Object, String> column = new TreeTableColumn<>(s("Type"));
        column.setCellValueFactory(new TypeFactory());
        column.setCellFactory(TypeCell::new);
        column.setPrefWidth(400);
        return column;
    }

    private TreeTableColumn<Object, Label> valueColumn() {
        final TreeTableColumn<Object, Label> column = new TreeTableColumn<>(s("Value"));
        column.setCellValueFactory(new ValueFactory());
        column.setCellFactory(c -> new ValueCell(c, beanEditor));
        column.setPrefWidth(500);
        return column;
    }

    void update(ProjectProfile profile) {
        setRoot(new TreeItem<>(profile, new ImageView(ROOT)));
        load();
    }

    BooleanProperty clearAllDisabled() {
        final BooleanProperty property = new SimpleBooleanProperty(getRoot().getChildren().isEmpty());
        rootProperty().addListener((observable, o, n) -> {
            final ObservableList<TreeItem<Object>> list = n.getChildren();
            list.addListener((ListChangeListener<TreeItem<Object>>) c -> property.set(n.getChildren().isEmpty()));
        });
        return property;
    }
}
