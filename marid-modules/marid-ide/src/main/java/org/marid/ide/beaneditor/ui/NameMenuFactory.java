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

package org.marid.ide.beaneditor.ui;

import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;
import org.marid.beans.MaridBeanXml;
import org.marid.ide.beaneditor.BeanEditor;
import org.marid.ide.beaneditor.BeanTreeConstants;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.project.ProjectProfile;

import java.nio.file.Path;
import java.util.Optional;

import static org.marid.l10n.L10nSupport.LS.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class NameMenuFactory implements BeanTreeConstants {

    public static ContextMenu menu(BeanEditor beanEditor, TreeItem<Object> item, Path path) {
        final ContextMenu menu = new ContextMenu();
        if (!path.getFileName().toString().endsWith(".xml")) {
            menu.getItems().add(subDir(beanEditor, path, item));
            menu.getItems().add(beanFile(beanEditor, path, item));
        } else {
            menu.getItems().add(beans(beanEditor, item));
        }
        return menu;
    }

    public static ContextMenu menu(BeanEditor beanEditor, TreeItem<Object> item, ProjectProfile profile) {
        final ContextMenu menu = new ContextMenu();
        menu.getItems().add(subDir(beanEditor, profile.getBeansDirectory(), item));
        menu.getItems().add(beanFile(beanEditor, profile.getBeansDirectory(), item));
        return menu;
    }

    private static Menu beans(BeanEditor editor, TreeItem<Object> parentItem) {
        final Menu m = new Menu(s("Add bean"));
        for (final MaridBeanXml xml : editor.getMetaBeans()) {
            final Image image = StringUtils.isBlank(xml.icon) ? editor.image(xml.type) : new Image(xml.icon, true);
            final MenuItem menuItem = new MenuItem(xml.type, new ImageView(image));
            menuItem.setOnAction(event -> {
                final BeanData beanData = new BeanData();
                beanData.name.set("name1");
                beanData.type.set(xml.type);
                final TreeItem<Object> item = new TreeItem<>(beanData, new ImageView(image));
                parentItem.getChildren().add(item);
                parentItem.setExpanded(true);
            });
            m.getItems().add(menuItem);
        }
        return m;
    }

    private static MenuItem subDir(BeanEditor editor, Path parent, TreeItem<Object> parentItem) {
        final MenuItem i = new MenuItem(s("Add subdirectory..."), new ImageView(DIR));
        i.setOnAction(event -> {
            final TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(s("Add subdirectory"));
            dialog.setHeaderText(s("Subdirectory name") + ":");
            final Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && StringUtils.isNotBlank(result.get())) {
                final Path newPath = parent.resolve(result.get());
                final TreeItem<Object> newFolderItem = new TreeItem<>(newPath, new ImageView(DIR));
                parentItem.getChildren().add(newFolderItem);
                parentItem.setExpanded(true);
            }
        });
        return i;
    }

    private static MenuItem beanFile(BeanEditor editor, Path parent, TreeItem<Object> parentItem) {
        final MenuItem i = new MenuItem(s("Add beans file..."), new ImageView(FILE));
        i.setOnAction(event -> {
            final TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(s("Add beans file"));
            dialog.setHeaderText(s("Beans file name") + ":");
            final Optional<String> result = dialog.showAndWait();
            if (result.isPresent() && StringUtils.isNotBlank(result.get())) {
                final String fileName = result.get().endsWith(".xml") ? result.get() : result.get() + ".xml";
                final Path newPath = parent.resolve(fileName);
                final TreeItem<Object> newFolderItem = new TreeItem<>(newPath, new ImageView(FILE));
                parentItem.getChildren().add(newFolderItem);
                parentItem.setExpanded(true);
            }
        });
        return i;
    }
}
