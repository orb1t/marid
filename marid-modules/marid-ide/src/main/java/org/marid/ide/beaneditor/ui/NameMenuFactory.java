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

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.property.StringProperty;
import javafx.beans.value.WritableObjectValue;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.marid.beans.MaridBeanXml;
import org.marid.ide.beaneditor.BeanEditor;
import org.marid.ide.beaneditor.BeanTreeConstants;
import org.marid.ide.beaneditor.BeanTreeUtils;
import org.marid.ide.beaneditor.ClassData;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.beaneditor.data.RefValue;
import org.marid.ide.project.ProjectProfile;

import java.beans.Introspector;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
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

    public static ContextMenu menu(BeanEditor beanEditor, TreeItem<Object> item, BeanData beanData) {
        final ContextMenu menu = new ContextMenu();
        final Set<BeanData> beans = BeanTreeUtils.beans(item);
        final ClassData classData = beanEditor.classData(beanData.type.get());
        final Map<String, Method> procedures = classData.getProcedures();
        if (!procedures.isEmpty()) {
            final Menu initMenu = new Menu(s("Initialize methods"));
            procedures.forEach((k, v) -> {
                final MenuItem menuItem = new MenuItem(k, glyphIcon(beanEditor.accessorIcon(v.getModifiers()), 16));
                menuItem.setOnAction(event -> {
                    beanData.initMethod.set(k);
                    if (beanData.destroyMethod.isEqualTo(k).get()) {
                        beanData.destroyMethod.set(null);
                    }
                });
                initMenu.getItems().add(menuItem);
            });
            menu.getItems().add(initMenu);
            final Menu destroyMenu = new Menu(s("Destroy methods"));
            procedures.forEach((k, v) -> {
                final MenuItem menuItem = new MenuItem(k, glyphIcon(beanEditor.accessorIcon(v.getModifiers()), 16));
                menuItem.setOnAction(event -> {
                    beanData.destroyMethod.set(k);
                    if (beanData.initMethod.isEqualTo(k).get()) {
                        beanData.initMethod.set(null);
                    }
                });
                destroyMenu.getItems().add(menuItem);
            });
            menu.getItems().add(destroyMenu);
        }
        final Map<StringProperty, Set<Method>> factoryMethods = beans.stream()
                .filter(b -> b != beanData)
                .flatMap(b -> beanEditor.classData(b.type.get()).getFactoryMethods().values().stream()
                        .filter(m -> classData.isAssignableFrom(beanEditor.classData(m.getReturnType().getName())))
                        .map(m -> Pair.of(b, m)))
                .collect(groupingBy(
                        p -> p.getKey().name,
                        () -> new TreeMap<>(comparing(WritableObjectValue::get)),
                        mapping(Pair::getValue, toCollection(() -> new TreeSet<>(comparing(Method::getName))))));
        if (!factoryMethods.isEmpty()) {
            if (!menu.getItems().isEmpty()) {
                menu.getItems().add(new SeparatorMenuItem());
            }
            final Menu bindMenu = new Menu(s("Bind"), glyphIcon(FontAwesomeIcon.LINK, 16));
            factoryMethods.forEach((nameProperty, methods) -> {
                final Menu m = new Menu(nameProperty.get());
                methods.forEach(method -> {
                    final Image image = beanEditor.image(method.getReturnType().getName());
                    final MenuItem menuItem = new MenuItem(method.getName(), new ImageView(image));
                    menuItem.setOnAction(event -> {
                        beanData.factoryBean.bind(nameProperty);
                        beanData.factoryMethod.set(method.getName());
                    });
                    m.getItems().add(menuItem);
                });
                bindMenu.getItems().add(m);
            });
            menu.getItems().add(bindMenu);
        }
        if (beanData.factoryBean.isNotEmpty().get()) {
            if (!menu.getItems().isEmpty()) {
                menu.getItems().add(new SeparatorMenuItem());
            }
            final MenuItem menuItem = new MenuItem(s("Unbind"), glyphIcon(FontAwesomeIcon.UNLINK, 16));
            menuItem.setOnAction(event -> {
                beanData.factoryBean.unbind();
                beanData.factoryBean.set(null);
                beanData.factoryMethod.set(null);
            });
            menu.getItems().add(menuItem);
        }
        return menu;
    }

    private static Menu beans(BeanEditor editor, TreeItem<Object> parentItem) {
        final Menu m = new Menu(s("Add bean"));
        for (final MaridBeanXml xml : editor.getMetaBeans()) {
            final Image image = StringUtils.isBlank(xml.icon) ? editor.image(xml.type) : new Image(xml.icon, true);
            final MenuItem menuItem = new MenuItem(xml.type, new ImageView(image));
            menuItem.setOnAction(event -> {
                final BeanData beanData = new BeanData();
                final ClassData classData = editor.classData(xml.type);
                final String beanPrefix = Introspector.decapitalize(classData.getType().getSimpleName());
                final int maxBeanIndex = BeanTreeUtils.beans(parentItem)
                        .stream()
                        .map(b -> b.name.get())
                        .filter(n -> n.startsWith(beanPrefix))
                        .map(n -> n.substring(beanPrefix.length()))
                        .mapToInt(n -> NumberUtils.isDigits(n) ? Integer.parseInt(n) : 0)
                        .max()
                        .orElse(0) + 1;
                beanData.name.set(beanPrefix + maxBeanIndex);
                beanData.type.set(xml.type);
                final TreeItem<Object> item = new TreeItem<>(beanData, new ImageView(image));
                editor.getLoader().fillConstructorArg(classData, item, null);
                editor.getLoader().fillProperties(classData, item, null);
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
