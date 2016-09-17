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

package org.marid.dependant.beaneditor.beans.listeditor;

import javafx.scene.control.ToolBar;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.list.MaridListActions;
import org.marid.jfx.toolbar.MaridToolbar;
import org.marid.spring.xml.data.collection.DCollection;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
@Import({ListEditor.class})
public class ListEditorConfiguration {

    private final DCollection<?> list;
    private final Type type;

    public ListEditorConfiguration(DCollection<?> list, Type type) {
        this.list = list;
        this.type = type;
    }

    @Bean
    public DCollection<?> list() {
        return list;
    }

    @Bean
    public Type type() {
        return type;
    }

    @Bean
    @Qualifier("listEditor")
    public FxAction clearAction(ListEditor editor) {
        return MaridListActions.clearAction(editor);
    }

    @Bean
    @Qualifier("listEditor")
    public FxAction removeAction(ListEditor editor) {
        return MaridListActions.removeAction(editor);
    }

    @Bean
    public ToolBar listEditorToolbar(@Qualifier("listEditor") Map<String, FxAction> actionMap) {
        return new MaridToolbar(actionMap);
    }
}
