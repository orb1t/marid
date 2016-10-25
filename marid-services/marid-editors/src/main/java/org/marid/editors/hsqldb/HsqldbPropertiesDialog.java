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

package org.marid.editors.hsqldb;

import de.jensd.fx.glyphs.GlyphIcon;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;
import org.marid.jfx.panes.GenericGridPane;
import org.marid.spring.xml.BeanData;
import org.marid.spring.xml.BeanProp;
import org.marid.spring.xml.collection.DValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public class HsqldbPropertiesDialog extends Dialog<Runnable> {

    public HsqldbPropertiesDialog(BeanData beanData) {
        setTitle(s("HSQLDB properties dialog: %s", beanData.name.get()));
        initModality(Modality.APPLICATION_MODAL);
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final BeanProp dirProperty = beanData.properties.stream()
                .filter(p -> p.name.isEqualTo("directory").get())
                .findAny()
                .orElse(null);

        final List<Runnable> commitTasks = new ArrayList<>();
        final GridPane pane = new GenericGridPane();
        final AtomicInteger row = new AtomicInteger();

        if (dirProperty != null) {
            final GlyphIcon<?> icon = FontIcons.glyphIcon(FontIcon.M_STORE_MALL_DIRECTORY, 16);
            final TextField field = new TextField();
            if (dirProperty.data.get() instanceof DValue) {
                field.setText(dirProperty.data.get().toString());
            }
            pane.addRow(row.getAndIncrement(), new Label(s("Directory"), icon), field);
            commitTasks.add(() -> {
                final DValue value = new DValue();
                value.setValue(field.getText());
                dirProperty.setData(value);
            });
        }

        getDialogPane().setContent(pane);

        setResultConverter(type -> type.getButtonData() == OK_DONE ? () -> commitTasks.forEach(Runnable::run) : null);
    }
}
