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

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import static javafx.beans.binding.Bindings.createStringBinding;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@Component
public class WindowCommonEditorConfiguration extends Stage {

    private final Property widthProperty;
    private final Property heightProperty;
    private final Property xProperty;
    private final Property yProperty;
    private final double x;
    private final double y;

    @Autowired
    public WindowCommonEditorConfiguration(BeanData beanData) {
        super(StageStyle.UTILITY);
        xProperty = beanData.property("x").orElseGet(Property::new);
        yProperty = beanData.property("y").orElseGet(Property::new);
        widthProperty = beanData.property("width").orElseGet(Property::new);
        heightProperty = beanData.property("height").orElseGet(Property::new);
        if (NumberUtils.isNumber(xProperty.value.get())) {
            x = Double.parseDouble(xProperty.value.get());
        } else {
            x = 0;
        }
        if (NumberUtils.isNumber(yProperty.value.get())) {
            y = Double.parseDouble(yProperty.value.get());
        } else {
            y = 0;
        }
        double width = 100, height = 100;
        if (NumberUtils.isNumber(widthProperty.value.get())) {
            width = Double.parseDouble(widthProperty.value.get());
        }
        if (NumberUtils.isNumber(heightProperty.value.get())) {
            height = Double.parseDouble(heightProperty.value.get());
        }
        if (width < 400) {
            width = 300;
        }
        if (height < 300) {
            height = 300;
        }
        setAlwaysOnTop(true);
        final Button button = new Button(s("Click here to set values"));
        button.setOnAction(event -> {
            widthProperty.value.set(Double.toString(getWidth()));
            heightProperty.value.set(Double.toString(getHeight()));
            xProperty.value.set(Double.toString(getX()));
            yProperty.value.set(Double.toString(getY()));
        });
        final BorderPane pane = new BorderPane(button);
        final Label label = new Label();
        label.textProperty().bind(createStringBinding(
                () -> s("x = %f, y = %f, width = %f, height = %f", getX(), getY(), getWidth(), getHeight()),
                xProperty(), yProperty(), widthProperty(), heightProperty()));
        pane.setBottom(label);
        setScene(new Scene(pane, width, height));
    }


    @EventListener
    private void onStart(ContextStartedEvent event) {
        show();
    }
}
