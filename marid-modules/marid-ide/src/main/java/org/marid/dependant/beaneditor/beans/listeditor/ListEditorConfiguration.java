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
import org.marid.spring.annotation.Q;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov.
 */
@Configuration
public class ListEditorConfiguration {

    @Bean
    @Q(ListEditorConfiguration.class)
    public FxAction clearAction(ListEditor editor) {
        return MaridListActions.clearAction(editor);
    }

    @Bean
    @Q(ListEditorConfiguration.class)
    public FxAction removeAction(ListEditor editor) {
        return MaridListActions.removeAction(editor);
    }

    @Bean
    public ToolBar listEditorToolbar(@Q(ListEditorConfiguration.class) Map<String, FxAction> actionMap) {
        return new MaridToolbar(actionMap);
    }
}
