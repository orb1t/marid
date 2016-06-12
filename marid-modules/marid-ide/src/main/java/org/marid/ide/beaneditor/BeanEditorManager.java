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

package org.marid.ide.beaneditor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.ide.toolbar.IdeToolbarItem;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.inject.Provider;

import static org.marid.jfx.icons.FontIcon.D_PUZZLE;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class BeanEditorManager {

    @Bean
    @IdeMenuItem(menu = "File", text = "Bean editor", group = "fileBeanEditor2", icon = D_PUZZLE)
    @IdeToolbarItem(group = "file")
    public EventHandler<ActionEvent> beanEditorItem(Provider<BeanEditor> beanEditorProvider) {
        return event -> {
            final BeanEditor beanEditor = beanEditorProvider.get();
            beanEditor.show();
        };
    }
}
