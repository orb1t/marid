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

package org.marid.ide.common;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import org.marid.jfx.action.FxAction;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.type.MethodMetadata;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class SpecialActions {

    private final EnumMap<IdeSpecialAction, FxAction> actionMap = new EnumMap<>(IdeSpecialAction.class);

    @Autowired
    public SpecialActions(@Qualifier("specialAction") Map<String, FxAction> actionMap, GenericApplicationContext context) {
        actionMap.forEach((name, action) -> {
            final AnnotatedBeanDefinition definition = (AnnotatedBeanDefinition) context.getBeanDefinition(name);
            final MethodMetadata metadata = definition.getFactoryMethodMetadata();
            final Map<String, Object> params = metadata.getAnnotationAttributes(SpecialAction.class.getName());
            final SpecialAction specialAction = synthesizeAnnotation(params, SpecialAction.class, null);
            this.actionMap.put(specialAction.value(), action);
        });
        checkState(this.actionMap.keySet().equals(EnumSet.allOf(IdeSpecialAction.class)));
    }

    public void set(IdeSpecialAction action, Node node, EventHandler<ActionEvent> eventHandler) {
        node.focusedProperty().addListener((observable, oldValue, newValue) -> {
            final FxAction fxAction = actionMap.get(action);
            if (newValue) {
                fxAction.setDisabled(false);
                fxAction.setEventHandler(eventHandler);
            } else {
                fxAction.setEventHandler(event -> {});
                fxAction.setDisabled(true);
            }
        });
    }
}
