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
import org.marid.Ide;
import org.marid.jfx.icons.FontIcon;
import org.marid.jfx.icons.FontIcons;
import org.marid.jfx.panes.GenericGridPane;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.Property;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static javafx.scene.control.ButtonBar.ButtonData.OK_DONE;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
public class HsqldbPropertiesEditorConfiguration {

    private final BeanData beanData;

    @Autowired
    public HsqldbPropertiesEditorConfiguration(BeanData beanData) {
        this.beanData = beanData;
    }

    @Bean
    public Dialog<Runnable> dialog() {
        final Property dirProperty = beanData.properties.stream()
                .filter(p -> p.name.isEqualTo("directory").get())
                .findAny()
                .orElse(null);

        final List<Runnable> commitTasks = new ArrayList<>();
        final GridPane pane = new GenericGridPane();
        final AtomicInteger row = new AtomicInteger();

        if (dirProperty != null) {
            final GlyphIcon<?> icon = FontIcons.glyphIcon(FontIcon.M_STORE_MALL_DIRECTORY, 16);
            final TextField field = new TextField(dirProperty.value.get());
            pane.addRow(row.getAndIncrement(), new Label(s("Directory"), icon), field);
            commitTasks.add(() -> dirProperty.value.set(field.getText()));
        }

        final Dialog<Runnable> dialog = new Dialog<>();
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.initOwner(Ide.primaryStage);
        dialog.setTitle(s("HSQLDB properties: %s", beanData.name.get()));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(pane);
        dialog.setResultConverter(p -> p.getButtonData() == OK_DONE ? () -> commitTasks.forEach(Runnable::run) : null);
        return dialog;
    }

    @Bean
    public ApplicationListener<ContextStartedEvent> onStartEvent(Dialog<Runnable> dialog) {
        return event -> {
            final Optional<Runnable> runnable = dialog.showAndWait();
            if (runnable.isPresent()) {
                runnable.get().run();
            }
        };
    }
}
