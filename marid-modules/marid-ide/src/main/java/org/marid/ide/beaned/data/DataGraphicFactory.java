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

package org.marid.ide.beaned.data;

import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import org.marid.ide.beaned.BeanTree;
import org.marid.logging.LogSupport;

import java.lang.reflect.Method;

/**
 * @author Dmitry Ovchinnikov
 */
public class DataGraphicFactory implements LogSupport {

    public static Node getGraphic(BeanTree beanTree, BeanContext beanContext, Data data) {
        try {
            final Method method = DataGraphicFactory.class.getDeclaredMethod("graphic",
                    BeanTree.class, BeanContext.class, data.getClass());
            return (Node) method.invoke(null, beanTree, beanContext, data);
        } catch (NoSuchMethodException x) {
            return null;
        } catch (ReflectiveOperationException x) {
            throw new IllegalStateException(x);
        }
    }

    static Node graphic(BeanTree beanTree, BeanContext beanContext, BeanData data) {
        final HBox hBox = new HBox(5);
        if (data.getFactoryBean() != null) {
            final Label label = new Label("factoryBean: ");
            label.setStyle("-fx-font-weight: bold");
            hBox.getChildren().add(label);
            hBox.getChildren().add(new Label(data.getFactoryBean()));
        }
        if (data.getFactoryMethod() != null) {
            if (!hBox.getChildren().isEmpty()) {
                hBox.getChildren().add(new Separator(Orientation.VERTICAL));
            }
            final Label label = new Label("factoryMethod: ");
            label.setStyle("-fx-font-weight: bold");
            hBox.getChildren().add(label);
            hBox.getChildren().add(new Label(data.getFactoryMethod()));
        }
        return hBox;
    }
}
