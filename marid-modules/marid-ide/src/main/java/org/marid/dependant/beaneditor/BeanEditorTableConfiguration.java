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

package org.marid.dependant.beaneditor;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import org.marid.ide.panes.main.IdePane;
import org.marid.ide.panes.tabs.IdeTabPane;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.ScrollPanes;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;
import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.l10n.L10n.s;
import static org.marid.misc.Builder.build;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class BeanEditorTableConfiguration {

    @Bean
    public ToolBar beanEditorToolbar(BeanEditorActions actions) {
        return new ToolbarBuilder()
                .add(s("Edit..."), M_EDIT, actions::onEdit, actions.itemActionDisabled)
                .addSeparator()
                .add(s("Remove"), O_REPO_DELETE, actions::onDelete, actions.itemActionDisabled)
                .add(s("Clear"), M_CLEAR_ALL, actions::onClear)
                .addSeparator()
                .add(s("Browse"), O_BROWSER, actions::onBrowse, actions.itemActionDisabled)
                .addSeparator()
                .add(s("Actions"), M_CREATE, actions::onShowPopup, actions.itemActionDisabled)
                .build();
    }

    @Bean
    public BorderPane beanEditor(BeanEditorTable table, ToolBar beanEditorToolbar, AnnotationConfigApplicationContext context) {
        final BorderPane pane = new BorderPane();
        pane.sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                context.close();
            }
        });
        pane.setCenter(ScrollPanes.scrollPane(table));
        pane.setTop(beanEditorToolbar);
        return pane;
    }

    @Bean
    public Tab tab(ProjectProfile profile, IdeTabPane tabPane, BorderPane beanEditor, Path beanFilePath) {
        final Path relativePath = profile.getBeansDirectory().relativize(beanFilePath);
        final Tab tab = new Tab(s("[%s]: %s", profile, relativePath), beanEditor);
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        return tab;
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public TableView<Entry<String, BeanDefinition>> beans(BeanMetaInfoProvider beanMetaInfoProvider) {
        final ObservableList<Entry<String, BeanDefinition>> definitions = beanMetaInfoProvider.beans().entrySet().stream()
                .filter(e -> e.getValue().isAbstract())
                .filter(e -> e.getValue().getFactoryBeanName() == null)
                .filter(e -> e.getValue().getFactoryMethodName() == null)
                .filter(e -> e.getValue().getBeanClassName() != null)
                .collect(Collectors.toCollection(FXCollections::observableArrayList));
        final TableView<Entry<String, BeanDefinition>> tableView = new TableView<>(definitions);
        tableView.setEditable(false);
        tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        tableView.setTableMenuButtonVisible(true);
        tableView.getColumns().add(build(new TableColumn<Entry<String, BeanDefinition>, String>(), col -> {
            col.setText(s("Name"));
            col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getKey()));
            col.setPrefWidth(300);
            col.setMaxWidth(600);
        }));
        tableView.getColumns().add(build(new TableColumn<Entry<String, BeanDefinition>, String>(), col -> {
            col.setText(s("Type"));
            col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getBeanClassName()));
            col.setPrefWidth(500);
            col.setMaxWidth(1000);
        }));
        tableView.getColumns().add(build(new TableColumn<Entry<String, BeanDefinition>, String>(), col -> {
            col.setText(s("Description"));
            col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getDescription()));
            col.setPrefWidth(500);
            col.setMaxWidth(2000);
        }));
        return tableView;
    }

    @Bean
    @Scope(SCOPE_PROTOTYPE)
    public Dialog<Entry<String, BeanDefinition>> beanBrowser(IdePane idePane, TableView<Entry<String, BeanDefinition>> beans) {
        final Dialog<Entry<String, BeanDefinition>> dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(idePane.getScene().getWindow());
        dialog.getDialogPane().setPrefSize(1024, 768);
        dialog.setTitle(s("Bean browser"));
        dialog.setResizable(true);
        dialog.setResultConverter(param -> param.getButtonData() == OK_DONE ? beans.getSelectionModel().getSelectedItem() : null);
        dialog.getDialogPane().getButtonTypes().addAll(new ButtonType(s("Add"), OK_DONE), ButtonType.CANCEL);
        dialog.getDialogPane().setContent(ScrollPanes.scrollPane(beans));
        return dialog;
    }
}
