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

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.geometry.Dimension2D;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.marid.logging.LogSupport;
import org.marid.spring.beandata.BeanEditorContext;
import org.marid.spring.xml.BeanProp;
import org.marid.spring.xml.collection.DValue;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ScreenEditorStage extends Stage implements LogSupport {

    final BeanEditorContext context;
    final ObjectProperty<File> svgFile = new SimpleObjectProperty<>(this, "svgFile");
    final DoubleProperty zoom = new SimpleDoubleProperty(this, "zoom", 1.0);
    final ObjectProperty<Dimension2D> size = new SimpleObjectProperty<>(this, "size", new Dimension2D(800, 600));
    final StackPane stackPane;

    private final WebView webView;

    public ScreenEditorStage(BeanEditorContext context) {
        this.context = context;
        setTitle(context.getBeanData().getName());
        initStyle(StageStyle.DECORATED);
        initOwner(context.getPrimaryStage());
        initModality(Modality.NONE);
        final Dimension2D initialSize = initialSize();
        setScene(new Scene(stackPane = new StackPane(webView = webView())));
        webView.setPrefWidth(initialSize.getWidth());
        webView.setPrefHeight(initialSize.getHeight());
        setOnShown(event -> {
            context.getBeanData().property("zoom")
                    .filter(p -> p.data.get() instanceof DValue)
                    .map(p -> (DValue) p.data.get())
                    .filter(p -> p.value.isNotEmpty().get())
                    .map(v -> v.value.get())
                    .filter(NumberUtils::isParsable)
                    .ifPresent(v -> zoom.set(Double.parseDouble(v)));
            context.getBeanData().property("relativeLocation")
                    .filter(p -> p.data.get() instanceof DValue)
                    .map(p -> (DValue) p.data.get())
                    .filter(v -> v.value.isNotEmpty().get())
                    .map(v -> v.value.get())
                    .ifPresent(v -> {
                        final Path path = context.getProfileInfo().getSrcMainResources().resolve(Paths.get(v));
                        svgFile.set(path.toFile());
                    });
            new ScreenEditorUtilityWindow(this).show();
        });
        setOnCloseRequest(Event::consume);
        final InvalidationListener onResize = o -> {
            final Dimension2D newSize = new Dimension2D(webView.getWidth(), webView.getHeight());
            size.setValue(newSize);
        };
        webView.widthProperty().addListener(onResize);
        webView.heightProperty().addListener(onResize);
    }

    private WebView webView() {
        final WebView webView = new WebView();
        webView.getEngine().setJavaScriptEnabled(true);
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

    private Dimension2D initialSize() {
        double width = 800;
        double height = 600;
        final BeanProp widthProp = context.getBeanData().property("prefWidth").orElse(null);
        final BeanProp heightProp = context.getBeanData().property("prefHeight").orElse(null);
        if (widthProp != null && widthProp.data.get() instanceof DValue) {
            try {
                width = Double.parseDouble(((DValue) widthProp.data.get()).getValue());
            } catch (NumberFormatException | NullPointerException x) {
                // default width
            }
        }
        if (heightProp != null && heightProp.data.get() instanceof DValue) {
            try {
                height = Double.parseDouble(((DValue) heightProp.data.get()).getValue());
            } catch (NumberFormatException | NullPointerException x) {
                // default height
            }
        }
        return new Dimension2D(width, height);
    }
}
