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
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.marid.ide.beaneditor.BeanEditor;
import org.marid.ide.beaneditor.ClassData;
import org.marid.ide.beaneditor.data.BeanData;
import org.marid.ide.beaneditor.data.RefValue;
import org.marid.jfx.dialog.MaridDialog;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toCollection;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10nSupport.LS.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class ValueMenuFactory {

    public static ContextMenu menu(BeanEditor editor, TreeItem<Object> item, RefValue rv) {
        final ContextMenu contextMenu = new ContextMenu();
        if (rv.ref().isBound()) {
            final MenuItem menuItem = new MenuItem(s("Unbind"), glyphIcon(FontAwesomeIcon.UNLINK, 16));
            menuItem.setOnAction(event -> {
                rv.ref().unbind();
                rv.ref().set(null);
            });
            contextMenu.getItems().add(menuItem);
        }
        final Set<BeanData> beans = beans(item);
        final List<BeanData> assignableBeans = beans.stream().filter(e -> {
            final ClassData beanClassData = editor.classData(e.type.get());
            final ClassData targetClassData = editor.classData(rv.type().get());
            return targetClassData.isAssignableFrom(beanClassData);
        }).collect(Collectors.toList());
        if (!assignableBeans.isEmpty()) {
            final Menu menu = new Menu(s("Bind"), glyphIcon(FontAwesomeIcon.LINK, 16));
            for (final BeanData beanData : assignableBeans) {
                final Image image = editor.image(beanData.type.get());
                final MenuItem menuItem = new MenuItem(beanData.name.get(), new ImageView(image));
                menuItem.setOnAction(event -> rv.ref().bind(beanData.name));
                menu.getItems().add(menuItem);
            }
            contextMenu.getItems().add(menu);
        }
        if (!rv.ref().isBound()) {
            if (!contextMenu.getItems().isEmpty()) {
                contextMenu.getItems().add(new SeparatorMenuItem());
            }
            final MenuItem menuItem = new MenuItem(s("Edit value as an expression..."), glyphIcon(FontAwesomeIcon.SCRIBD, 16));
            menuItem.setOnAction(event -> {
                final ValueExpressionEditor expressionEditor = new ValueExpressionEditor(rv.value());
                final Optional<String> value = new MaridDialog<String>(editor, ButtonType.APPLY, ButtonType.CANCEL)
                        .title(s("Expression editor"))
                        .preferredSize(800, 600)
                        .with((d, p) -> p.setContent(expressionEditor))
                        .result(expressionEditor::accept)
                        .showAndWait();
                if (value.isPresent()) {
                    rv.value().set(value.get());
                }
            });
            contextMenu.getItems().add(menuItem);
        }
        return contextMenu;
    }

    private static Set<BeanData> beans(TreeItem<Object> item) {
        for (TreeItem<Object> i = item.getParent(); i != null; i = i.getParent()) {
            if (i.getParent() == null) {
                final List<TreeItem<Object>> list = new ArrayList<>();
                items(i, e -> e.getValue() instanceof BeanData, list);
                return list.stream()
                        .map(e -> (BeanData) e.getValue())
                        .collect(toCollection(() -> new TreeSet<>(comparing(e -> e.name.get()))));
            }
        }
        return Collections.emptySet();
    }

    private static void items(TreeItem<Object> item, Predicate<TreeItem<Object>> filter, List<TreeItem<Object>> list) {
        for (final TreeItem<Object> i : item.getChildren()) {
            if (filter.test(i)) {
                list.add(i);
            }
            items(i, filter, list);
        }
    }
}
