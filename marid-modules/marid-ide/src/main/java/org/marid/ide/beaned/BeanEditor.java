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

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.marid.beans.MaridBeanXml;
import org.marid.ide.Ide;
import org.marid.ide.beaned.data.BeanContext;
import org.marid.ide.beaned.data.BeanData;
import org.marid.ide.beaned.data.Data;
import org.marid.ide.project.ProjectProfile;
import org.marid.ide.timers.IdeTimers;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.menu.MenuContainerBuilder;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.spring.BeansSerializer;
import org.marid.spring.xml.Bean;
import org.marid.spring.xml.Beans;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.lang.model.element.ElementKind;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javafx.beans.binding.Bindings.createStringBinding;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.marid.ide.beaned.data.DataEditorFactory.newDialog;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class BeanEditor extends Stage implements L10nSupport, LogSupport {

    private final BeanContext beanContext;
    private final BeanTree beanTree;
    private final ObjectProperty<File> file = new SimpleObjectProperty<>();

    @Inject
    public BeanEditor(ProjectProfile profile, IdeTimers ideTimers) {
        beanContext = new BeanContext(profile);
        beanTree = new BeanTree(beanContext, ideTimers);
        getIcons().addAll(Ide.IMAGES);
        setScene(new Scene(getTreePane(), 1024, 768));
        titleProperty().bind(createStringBinding(this::title, file));
        setOnCloseRequest(event -> {
            try {
                beanContext.close();
            } catch (Exception x) {
                log(WARNING, "Unable to free resources", x);
            }
        });
    }

    public BorderPane getTreePane() {
        final ToolBar toolBar = new ToolBar();
        final MenuBar menuBar = new MenuBar();
        new MenuContainerBuilder()
                .menu("File", b -> b
                        .item("*Open...", MaterialIcon.OPEN_IN_NEW, "Ctrl+O", event -> open())
                        .item("*Save", MaterialIcon.SAVE, "Ctrl+S", event -> save())
                        .item("Save as...", null, event -> saveAs())
                        .separator()
                        .item("*Print", MaterialIcon.PRINT, "Ctrl+P", event -> print()))
                .menu("Beans", b -> b
                        .item("*Browse beans", MaterialIcon.ADD_BOX, contextMenu())
                        .last(a -> a.getProperties().put("menu", contextMenu()))
                        .item("*Clear all", MaterialIcon.CLEAR_ALL, event -> beanTree.getRoot().getChildren().clear())
                        .last(a -> a.disabledProperty().bind(Bindings.isEmpty(beanTree.getRoot().getChildren())))
                        .separator()
                        .item("*Edit...", MaterialDesignIcon.TABLE_EDIT, "F2", event -> beanTree.editItem(beanContext))
                        .last(a -> a.disabledProperty().bind(noSelection())))
                .menu("Window", b -> b
                        .item("*Refresh", MaterialDesignIcon.REFRESH, "F5", event -> beanTree.refresh()))
                .build(menuBar.getMenus()::add, toolBar.getItems());
        final VBox vBox = new VBox(menuBar, toolBar);
        final BorderPane pane = new BorderPane(ScrollPanes.scrollPane(beanTree), vBox, null, null, null);
        pane.setFocusTraversable(false);
        return pane;
    }

    private String title() {
        return String.format("[%s] %s", beanContext.profile, file.isNull().get() ? s("New") : file.get());
    }

    private BooleanBinding noSelection() {
        return Bindings.createBooleanBinding(() -> {
            final TreeItem<Data> treeItem = beanTree.getSelectionModel().getSelectedItem();
            return treeItem == null || newDialog(beanTree, beanContext, treeItem.getValue()) == null;
        }, beanTree.getSelectionModel().selectedIndexProperty());
    }

    private void save() {
        if (file.get() == null) {
            saveAs();
            return;
        }
        final Beans beans = new Beans();
        for (final TreeItem<Data> beanItem : beanContext.root.getChildren()) {
            if (beanItem.getValue() instanceof BeanData) {
                final BeanData data = (BeanData) beanItem.getValue();
                final Bean bean = new Bean();
                bean.name = data.getName();
                bean.factoryBean = isBlank(data.getFactoryBean()) ? null : data.getFactoryBean();
                bean.factoryMethod = isBlank(data.getFactoryMethod()) ? null : data.getFactoryMethod();
                bean.beanClass = data.getType();
                bean.initMethod = isBlank(data.getInitMethod()) ? null : data.getInitMethod();
                bean.destroyMethod = isBlank(data.getDestroyMethod()) ? null : data.getDestroyMethod();
                beans.beans.add(bean);
            }
        }
        try {
            BeansSerializer.serialize(beans, file.get());
        } catch (Exception x) {
            log(WARNING, "Unable to save {0}", x, file);
        }
    }

    private void saveAs() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(m("Select a file with beans"));
        fileChooser.setInitialDirectory(beanContext.profile.getBeansDirectory().toFile());
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Beans", ".xml"));
        final File file = fileChooser.showSaveDialog(this);
        if (file != null) {
            this.file.set(file);
            save();
        }
    }

    private void print() {
    }

    private void open() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(m("Select a file with beans"));
        fileChooser.setInitialDirectory(beanContext.profile.getBeansDirectory().toFile());
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Beans", ".xml"));
        final File file = fileChooser.showOpenDialog(this);
        if (file != null) {
            show();
        }
    }

    private ContextMenu contextMenu() {
        final ContextMenu contextMenu = new ContextMenu();
        final Pattern beanNamePattern = Pattern.compile("bean(\\d+)");
        for (final MaridBeanXml beanXml : beanContext.beansXmls) {
            if (beanXml.kind != ElementKind.CLASS) {
                continue;
            }
            final GlyphIcon<?> icon = BeanContext.icon(beanXml.icon, 16, OctIcon.CODE);
            final String text = beanXml.text == null ? beanXml.type : beanXml.text + ": " + beanXml.type;
            final MenuItem menuItem = new MenuItem(text, icon);
            menuItem.setOnAction(event -> {
                final int beanNum = beanContext.root.getChildren().stream()
                        .map(TreeItem::getValue)
                        .filter(BeanData.class::isInstance)
                        .map(BeanData.class::cast)
                        .map(bd -> beanNamePattern.matcher(bd.getName()))
                        .filter(Matcher::matches)
                        .map(m -> m.group(1))
                        .mapToInt(Integer::parseInt)
                        .max()
                        .orElse(0) + 1;
                    beanTree.addBean("bean" + beanNum, beanXml.type);
            });
            contextMenu.getItems().add(menuItem);
        }
        return contextMenu;
    }
}
