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

package org.marid.hmi.screen;

import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.controlsfx.control.StatusBar;
import org.marid.jfx.control.MaridLabel;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.spring.beandata.BeanEditorContext;
import org.marid.spring.xml.BeanProp;
import org.marid.spring.xml.collection.DValue;

import java.io.File;
import java.nio.file.Path;

import static javafx.beans.binding.Bindings.format;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcon.M_IMPORT_EXPORT;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public class ScreenEditorUtilityWindow extends Stage {

    private final BeanEditorContext context;
    private final ObjectProperty<Dimension2D> size;
    private final ObjectProperty<File> svgFile;
    private final DoubleProperty zoom;

    public ScreenEditorUtilityWindow(ScreenEditorStage stage) {
        super(StageStyle.UTILITY);
        context = stage.context;
        size = stage.size;
        svgFile = stage.svgFile;
        zoom = stage.zoom;
        initOwner(stage);
        setAlwaysOnTop(true);
        setOnCloseRequest(event -> stage.close());
        setScene(new Scene(mainPane(), 800, 300));
        setX(Screen.getPrimary().getBounds().getMinX());
        setY(Screen.getPrimary().getBounds().getMinY());
    }

    private BorderPane mainPane() {
        final BorderPane borderPane = new BorderPane();
        borderPane.setTop(toolBar());
        borderPane.setCenter(applyButton());
        borderPane.setBottom(statusBar());
        return borderPane;
    }

    private Button applyButton() {
        final Button button = new Button();
        button.textProperty().bind(ls("Apply"));
        button.setOnAction(event -> {
            final BeanProp locationProp = context.getBeanData().property("relativeLocation").orElse(null);
            final BeanProp prefWidthProp = context.getBeanData().property("prefWidth").orElse(null);
            final BeanProp prefHeightProp = context.getBeanData().property("prefHeight").orElse(null);
            final BeanProp zoomProp = context.getBeanData().property("zoom").orElse(null);
            if (locationProp != null && svgFile.isNotNull().get()) {
                final Path resourcesDir = context.getProfileInfo().getSrcMainResources();
                final Path relativePath = resourcesDir.relativize(svgFile.get().toPath());
                locationProp.setData(new DValue(relativePath.toString()));
            }
            if (size.isNotNull().get()) {
                if (prefWidthProp != null) {
                    prefWidthProp.setData(new DValue(Double.toString(size.get().getWidth() + 10)));
                }
                if (prefHeightProp != null) {
                    prefHeightProp.setData(new DValue(Double.toString(size.get().getHeight() + 10)));
                }
            }
            if (zoomProp != null) {
                zoomProp.setData(new DValue(Double.toString(zoom.get())));
            }
        });
        return button;
    }

    private ToolBar toolBar() {
        return new ToolbarBuilder()
                .add("Import resource", M_IMPORT_EXPORT, event -> {
                    final Path resourcesDir = context.getProfileInfo().getSrcMainResources();
                    final FileChooser chooser = new FileChooser();
                    chooser.setInitialDirectory(resourcesDir.toFile());
                    chooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("SVG", "*.svg"));
                    final File file = chooser.showOpenDialog(this);
                    if (file != null) {
                        svgFile.set(file);
                    }
                })
                .addSeparator()
                .add(new MaridLabel(), l -> l.format("%s: ", "Scale"))
                .add(new Spinner<Double>(), s -> {
                    s.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 10.0, zoom.get(), 0.1));
                    s.valueProperty().addListener((observable, oldValue, newValue) -> zoom.set(newValue));
                })
                .build();
    }

    private StatusBar statusBar() {
        final StatusBar statusBar = new StatusBar();
        statusBar.setPadding(new Insets(5));
        statusBar.setText("");
        statusBar.getLeftItems().add(build(new MaridLabel(), l -> l.format("%s: ", "File")));
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
        statusBar.getLeftItems().add(new Separator(Orientation.VERTICAL));
        statusBar.getLeftItems().add(build(new MaridLabel(), l -> l.format("%s: ", "Zoom")));
        statusBar.getLeftItems().add(build(new Label(), l -> l.textProperty().bind(format("%fx", zoom))));
        statusBar.getLeftItems().add(new Separator(Orientation.VERTICAL));
        statusBar.getLeftItems().add(build(new MaridLabel(), l -> l.format("%s: ", "Size")));
        statusBar.getLeftItems().add(build(new Label(), l -> l.textProperty().bind(Bindings.createStringBinding(() -> {
            final Dimension2D d = size.get();
            return String.format("%dx%d", Math.round(d.getWidth()), Math.round(d.getHeight()));
        }, size))));
        statusBar.getLeftItems().stream()
                .filter(Control.class::isInstance)
                .map(Control.class::cast)
                .forEach(c -> c.setPadding(new Insets(5)));
        return statusBar;
    }
}
