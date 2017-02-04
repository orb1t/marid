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

package org.marid.dependant.beaneditor.veluemenu;

import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.WritableValue;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.valueeditor.ValueEditorConfiguration;
import org.marid.dependant.beaneditor.valueeditor.ValueEditorParams;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.menu.MaridMenu;
import org.marid.spring.beans.ConditionalBean;
import org.marid.spring.dependant.DependantConfiguration;
import org.marid.spring.xml.DElement;
import org.marid.spring.xml.DValue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ResolvableType;

import java.util.Map;

import static org.marid.jfx.icons.FontIcon.M_CLEAR;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
public class ValueContext extends DependantConfiguration<ValueParams> {

    @Bean
    public WritableValue<DElement<?>> element() {
        return param.element;
    }

    @Bean
    public ResolvableType type() {
        return param.type;
    }

    @Bean
    public ValueParams params() {
        return param;
    }

    @Bean
    public ObservableStringValue name() {
        return param.name;
    }

    @Bean
    @Qualifier("value")
    public ConditionalBean<FxAction> clearItem(WritableValue<DElement<?>> element) {
        return new ConditionalBean<>(element.getValue() == null ? null :
                new FxAction("ops", "Value")
                        .bindText("Clear value")
                        .setIcon(M_CLEAR)
                        .setEventHandler(event -> element.setValue(null))
        );
    }

    @Bean
    @Qualifier("value")
    public FxAction editItem(WritableValue<DElement<?>> element, IdeDependants dependants, ResolvableType type) {
        return new FxAction("ops", "Value")
                .bindText("Clear value")
                .setIcon(M_CLEAR)
                .setEventHandler(event -> {
                    final DValue value;
                    if (element.getValue() instanceof DValue) {
                        value = (DValue) element.getValue();
                    } else {
                        element.setValue(value = new DValue());
                    }
                    dependants.start(ValueEditorConfiguration.class, new ValueEditorParams(type, value), context -> {
                        context.setId("valueEditor");
                        context.setDisplayName("Value Editor");
                    });
                });
    }

    @Bean
    public ContextMenu contextMenu(@Qualifier("value") Map<String, FxAction> actionMap) {
        final MaridMenu menu = new MaridMenu(actionMap);
        return new ContextMenu(menu.getMenus().stream().flatMap(m -> m.getItems().stream()).toArray(MenuItem[]::new));
    }
}

