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
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.SpinnerValueFactory.DoubleSpinnerValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import org.controlsfx.control.StatusBar;
import org.marid.jfx.control.MaridLabel;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.logging.LogSupport;
import org.marid.spring.beandata.BeanEditorContext;
import org.marid.spring.xml.BeanProp;
import org.marid.spring.xml.collection.DValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static javafx.beans.binding.Bindings.format;
import static org.marid.jfx.icons.FontIcon.D_SYNC;
import static org.marid.jfx.icons.FontIcon.M_IMPORT_EXPORT;
import static org.marid.l10n.L10n.s;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public class ScreenEditorStage extends Stage implements LogSupport {

    private final BorderPane mainPane;
    private final BeanEditorContext context;
    private final ObjectProperty<File> svgFile = new SimpleObjectProperty<>(this, "svgFile");
    private final DoubleProperty zoom = new SimpleDoubleProperty(this, "zoom", 1.0);
    private final ObjectProperty<Dimension2D> size = new SimpleObjectProperty<>(this, "size", new Dimension2D(800, 600));

    public ScreenEditorStage(BeanEditorContext context) {
        this.context = context;
        setTitle(context.getBeanData().getName());
        setScene(new Scene(mainPane = mainPane(), 800, 600));
        setOnShown(event -> {
            final BeanProp prop = context.getBeanData().property("relativeLocation").orElse(null);
            if (prop != null) {
                if (prop.data.get() instanceof DValue) {
                    final DValue value = (DValue) prop.data.get();
                    if (value.value.isNotEmpty().get()) {
                        final Path relativePath = Paths.get(value.value.get());
                        final Path path = context.getProfileInfo().getSrcMainResources().resolve(relativePath);
                        svgFile.set(path.toFile());
                    }
                }
            }
        });
    }

    private BorderPane mainPane() {
        final BorderPane pane = new BorderPane();
        pane.setTop(toolBar());
        pane.setBottom(statusBar());
        pane.setCenter(webView());
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
                        svgFile.set(file);
                    }
                })
                .addSeparator()
                .add(new Label(s("Scale") + ":"), l -> {})
                .add(new Spinner<Double>(), s -> {
                    s.setValueFactory(new DoubleSpinnerValueFactory(0.1, 10.0, 1.0, 0.1));
                    s.valueProperty().addListener((observable, oldValue, newValue) -> zoom.set(newValue));
                })
                .addSeparator()
                .add("Apply", D_SYNC, event -> {
                    final BeanProp locationProp = context.getBeanData().property("relativeLocation").orElse(null);
                    final BeanProp prefWidthProp = context.getBeanData().property("prefWidth").orElse(null);
                    final BeanProp prefHeightProp = context.getBeanData().property("prefHeight").orElse(null);
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

    private WebView webView() {
        final WebView webView = new WebView();
        webView.getEngine().setJavaScriptEnabled(true);
        webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            switch (newValue) {
                case READY:
                case SUCCEEDED:
                    size.set(size(webView, zoom.get()));
                    break;
            }
        });
        size.addListener((observable, oldValue, newValue) -> {
            final double width = newValue.getWidth();
            final double height = newValue.getHeight();
            webView.setPrefWidth(width + 70);
            webView.setPrefHeight(height + 70);
            final double oldX = getX();
            final double oldY = getY();
            final double oldWidth = mainPane.getWidth();
            final double oldHeight = mainPane.getHeight();
            final double newWidth = mainPane.prefWidth(-1);
            final double newHeight = mainPane.prefHeight(-1);
            final double oldWindowWidth = getWidth();
            final double oldWindowHeight = getHeight();
            setX(oldX - (newWidth - oldWidth) / 2);
            setY(oldY - (newHeight - oldHeight) / 2);
            setWidth(oldWindowWidth + newWidth - oldWidth);
            setHeight(oldWindowHeight + newHeight - oldHeight);
        });
        zoom.addListener((observable, oldValue, newValue) -> {
            size.set(size(webView, zoom.get()));
        });
        svgFile.addListener((observable, oldValue, newValue) -> {
            try {
                final URL url = newValue.toURI().toURL();
                webView.getEngine().load(url.toExternalForm());
                webView.zoomProperty().bindBidirectional(zoom);
            } catch (Exception x) {
                log(WARNING, "Unable to load {0}", x, newValue);
            }
        });
        return webView;
    }

    static Dimension2D size(WebView webView, double zoom) {
        final Document document = webView.getEngine().getDocument();
        if (document == null) {
            return new Dimension2D(800 * zoom, 600 * zoom);
        }
        final Element svg = document.getDocumentElement();
        final String viewBox = svg.getAttribute("viewBox");
        if (viewBox == null) {
            return new Dimension2D(800 * zoom, 600 * zoom);
        }
        final String[] parts = viewBox.split("\\s");
        if (parts.length != 4) {
            return new Dimension2D(800 * zoom, 600 * zoom);
        }
        final double[] elements = Stream.of(parts).mapToDouble(Double::parseDouble).toArray();
        return new Dimension2D(elements[2] * zoom, elements[3] * zoom);
    }
}
