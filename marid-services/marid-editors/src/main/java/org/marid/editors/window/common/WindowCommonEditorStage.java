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

package org.marid.editors.window.common;

import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanProp;
import org.marid.spring.xml.collection.DValue;

import static javafx.beans.binding.Bindings.format;
import static org.marid.jfx.icons.FontIcon.*;
import static org.marid.jfx.icons.FontIcons.glyphIcon;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public class WindowCommonEditorStage extends Stage {

    private final BeanProp widthProperty;
    private final BeanProp heightProperty;
    private final BeanProp xProperty;
    private final BeanProp yProperty;
    private final double x;
    private final double y;

    private double getValue(BeanProp prop, double defValue) {
        if (prop.data.get() instanceof DValue) {
            try {
                final DValue value = (DValue) prop.data.get();
                return Double.parseDouble(value.getValue());
            } catch (NullPointerException | NumberFormatException x) {
                return defValue;
            }
        }
        return defValue;
    }

    private void setValue(BeanProp prop, double value) {
        prop.data.setValue(new DValue(Double.toString(value)));
    }

    public WindowCommonEditorStage(BeanData beanData) {
        super(StageStyle.UTILITY);
        xProperty = beanData.property("x").orElseGet(BeanProp::new);
        yProperty = beanData.property("y").orElseGet(BeanProp::new);
        widthProperty = beanData.property("width").orElseGet(BeanProp::new);
        heightProperty = beanData.property("height").orElseGet(BeanProp::new);
        x = getValue(xProperty, 0);
        y = getValue(yProperty, 0);
        double width = getValue(widthProperty, 400);
        double height = getValue(heightProperty, 300);
        if (width < 400) {
            width = 400;
        }
        if (height < 300) {
            height = 300;
        }
        setAlwaysOnTop(true);
        final BorderPane pane = new BorderPane();
        {
            final Button button = new Button(s("Apply"), glyphIcon(D_CHECK_ALL, 24));
            button.setOnAction(event -> {
                setValue(xProperty, getX());
                setValue(yProperty, getY());
                setValue(widthProperty, getWidth());
                setValue(heightProperty, getHeight());
            });
            pane.setCenter(button);
        }
        {
            final Label label = new Label();
            label.textProperty().bind(format("X: %f", xProperty()));
            pane.setTop(new BorderPane(label, null, glyphIcon(D_ARROW_TOP_RIGHT, 24), null, glyphIcon(D_ARROW_TOP_LEFT, 24)));
        }
        {
            final Label label = new Label();
            label.textProperty().bind(format("WIDTH: %f", widthProperty()));
            pane.setBottom(new BorderPane(label, null, glyphIcon(D_ARROW_BOTTOM_RIGHT, 24), null, glyphIcon(D_ARROW_BOTTOM_LEFT, 24)));
        }
        {
            final Label label = new Label();
            label.textProperty().bind(format("Y: %f", yProperty()));
            label.setRotate(-90);
            final VBox box = new VBox(new Group(label));
            box.setAlignment(Pos.CENTER);
            pane.setLeft(box);
        }
        {
            final Label label = new Label();
            label.textProperty().bind(format("HEIGHT: %f", heightProperty()));
            label.setRotate(90);
            final VBox box = new VBox(new Group(label));
            box.setAlignment(Pos.CENTER);
            pane.setRight(box);
        }
        setScene(new Scene(pane, width, height));
        setX(x);
        setY(y);
    }
}
