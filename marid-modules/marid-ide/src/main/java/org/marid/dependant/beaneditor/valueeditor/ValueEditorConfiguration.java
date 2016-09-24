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

package org.marid.dependant.beaneditor.valueeditor;

import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.marid.Ide;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.l10n.L10n;
import org.marid.spring.xml.data.collection.DValue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.lang.reflect.Type;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class ValueEditorConfiguration {

    @Bean
    public DValue value(Environment environment) {
        return environment.getProperty("value", DValue.class);
    }

    @Bean
    public Type type(Environment environment) {
        return environment.getProperty("type", Type.class);
    }

    @Bean
    @Qualifier("valueEditor")
    public TextArea textArea(DValue value) {
        return new TextArea(value.getValue());
    }

    @Bean
    public AutoCloseable textAreaDestroyer(@Qualifier("valueEditor") TextArea textArea, DValue value) {
        return () -> value.value.set(textArea.getText());
    }

    @Bean
    @Qualifier("valueEditor")
    public BorderPane pane(@Qualifier("valueEditor") TextArea textArea) {
        return new BorderPane(new MaridScrollPane(textArea));
    }

    @Bean(initMethod = "show")
    public Stage valueEditorStage(@Qualifier("valueEditor") BorderPane pane) {
        final Stage stage = new Stage();
        stage.initOwner(Ide.primaryStage);
        stage.setScene(new Scene(pane, 800, 600));
        stage.setTitle(L10n.s("Value editor"));
        return stage;
    }
}
