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

package org.marid.ide.beaned;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeItem;
import javafx.stage.WindowEvent;
import org.marid.ide.beaned.data.BeanData;
import org.marid.ide.beaned.data.Data;
import org.marid.ide.beaned.data.RefData;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.ide.toolbar.IdeToolbarItem;
import org.marid.l10n.L10nSupport;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Provider;
import java.util.*;

/**
 * @author Dmitry Ovchinnikov
 */
@ApplicationScoped
public class BeanEditorManager implements L10nSupport {

    final Set<BeanEditor> beanEditors = new LinkedHashSet<>();
    private final ListChangeListener<TreeItem<Data>> changeListener = c -> {
        while (c.next()) {
            c.getAddedSubList().forEach(item -> {
                item.getChildren().addListener(this.changeListener);
                final ChangeListener<String> changeListener = (observable, oldValue, newValue) -> {
                    for (final BeanEditor e : beanEditors) {
                        processBeanRename(oldValue, newValue, e.beanTree.getRoot().getChildren());
                    }
                };
                if (item.getValue() instanceof BeanData) {
                    item.getValue().nameProperty().addListener(changeListener);
                }
            });
        }
    };

    @Produces
    @IdeMenuItem(menu = "File", text = "Bean editor", group = "fileBeanEditor", mdIcons = {MaterialDesignIcon.PUZZLE})
    @IdeToolbarItem(group = "file")
    public EventHandler<ActionEvent> beanEditor(Provider<BeanEditor> beanEditorProvider) {
        return event -> {
            final BeanEditor beanEditor = beanEditorProvider.get();
            beanEditors.add(beanEditor);
            beanEditor.addEventHandler(WindowEvent.WINDOW_HIDING, e -> beanEditors.remove(beanEditor));
            setUpListeners(beanEditor);
            beanEditor.show();
        };
    }

    private void setUpListeners(BeanEditor beanEditor) {
        beanEditor.beanTree.getRoot().getChildren().addListener(changeListener);
    }

    private void processBeanRename(String oldName, String newName, Collection<TreeItem<Data>> list) {
        list.forEach(item -> {
            final List<StringProperty> references = new ArrayList<>();
            if (item.getValue() instanceof BeanData) {
                references.add(((BeanData) item.getValue()).factoryBeanProperty());
            } else if (item.getValue() instanceof RefData) {
                references.add(((RefData) item.getValue()).refProperty());
            }
            references.forEach(reference -> {
                if (oldName.equals(reference.get())) {
                    reference.set(newName);
                }
            });
            processBeanRename(oldName, newName, item.getChildren());
        });
    }
}
