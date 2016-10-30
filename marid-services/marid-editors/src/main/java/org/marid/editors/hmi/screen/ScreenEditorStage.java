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

package org.marid.editors.hmi.screen;

import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.logging.LogSupport;
import org.marid.spring.beandata.BeanEditorContext;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanProp;
import org.marid.spring.xml.collection.DValue;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;

import static javafx.scene.control.Alert.AlertType.ERROR;
import static javafx.scene.control.ButtonType.OK;
import static org.marid.jfx.icons.FontIcon.M_IMPORT_EXPORT;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public class ScreenEditorStage extends Stage implements LogSupport {

    private final BeanEditorContext context;
    private final ObjectProperty<File> svgFile = new SimpleObjectProperty<>(this, "svgFile");

    public ScreenEditorStage(BeanEditorContext context) {
        this.context = context;
        initOwner(context.getPrimaryStage());
        initModality(Modality.WINDOW_MODAL);
        setTitle(context.getBeanData().getName());
        setScene(new Scene(mainPane(), 800, 600));
    }

    private BorderPane mainPane() {
        final BorderPane pane = new BorderPane();
        pane.setTop(toolBar());
        pane.setBottom(statusBar());
        pane.setCenter(new MaridScrollPane(webView()));
        return pane;
    }

    private ToolBar toolBar() {
        return new ToolbarBuilder()
                .add("Import resource", M_IMPORT_EXPORT, event -> {
                    final Path resourcesDir = context.getProfileInfo().getSrcMainResources();
                    final FileChooser chooser = new FileChooser();
                    chooser.setInitialDirectory(resourcesDir.toFile());
                    chooser.getExtensionFilters().addAll(new ExtensionFilter(s("SVG"), "*.svg"));
                    final File file = chooser.showOpenDialog(this);
                    if (file != null) {
                        try {
                            final Path relative = resourcesDir.relativize(file.toPath());
                            final BeanData beanData = context.getBeanData();
                            final BeanProp prop = beanData.property("relativeLocation").orElse(null);
                            if (prop != null) {
                                prop.setData(new DValue(relative.toString()));
                                svgFile.set(file);
                            }
                        } catch (IllegalArgumentException x) {
                            final Alert alert = new Alert(ERROR, m("Selected file is not a resource"), OK);
                            alert.initOwner(this);
                            alert.initModality(Modality.WINDOW_MODAL);
                            alert.showAndWait();
                        }
                    }
                })
                .build();
    }

    private StatusBar statusBar() {
        final StatusBar statusBar = new StatusBar();
        statusBar.setText("");
        statusBar.getLeftItems().add(build(new Label(s("File") + ":"), l -> {}));
        statusBar.getLeftItems().add(build(new Label(), l -> {
            l.textProperty().bind(Bindings.createStringBinding(() -> {
                if (svgFile.isNull().get()) {
                    return "";
                }
                final File file = svgFile.get();
                final Path relative = context.getProfileInfo().getSrcMainResources().relativize(file.toPath());
                return relative.toString();
            }, svgFile));
        }));
        return statusBar;
    }

    private WebView webView() {
        final WebView webView = new WebView();
        svgFile.addListener((observable, oldValue, newValue) -> {
            try {
                final URL url = newValue.toURI().toURL();
                webView.getEngine().load(url.toExternalForm());
            } catch (Exception x) {
                log(WARNING, "Unable to load {0}", x, newValue);
            }
        });
        return webView;
    }
}
