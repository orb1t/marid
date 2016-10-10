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

import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import org.marid.Ide;
import org.marid.jfx.dialog.MaridDialog;
import org.marid.jfx.panes.MaridScrollPane;
import org.marid.spring.xml.collection.DValue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class ValueEditorConfiguration {

    @Bean
    @Qualifier("valueEditor")
    public TextArea textArea(DValue value) {
        return new TextArea(value.getValue());
    }

    @Bean(initMethod = "show")
    public Dialog<String> valueEditorStage(@Qualifier("valueEditor") TextArea area, DValue value) {
        return new MaridDialog<String>(Ide.primaryStage)
                .title("Value editor")
                .preferredSize(800, 600)
                .with((d, p) -> p.setContent(new MaridScrollPane(area)))
                .result(() -> {
                    final String v = area.getText();
                    value.setValue(v);
                    return v;
                });
    }
}
