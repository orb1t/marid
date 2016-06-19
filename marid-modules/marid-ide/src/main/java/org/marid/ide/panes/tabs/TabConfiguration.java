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

package org.marid.ide.panes.tabs;

import javafx.scene.control.Tab;
import org.marid.ide.panes.filebrowser.BeanFileBrowserPane;
import org.marid.ide.panes.logging.LoggingTable;
import org.marid.jfx.ScrollPanes;
import org.marid.l10n.L10nSupport;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class TabConfiguration implements L10nSupport {

    @Bean
    @Qualifier("ideTab")
    @Order(1)
    public Tab logTab(LoggingTable loggingTable) {
        final Tab tab = new Tab(s("Log"), ScrollPanes.scrollPane(loggingTable));
        tab.setClosable(false);
        return tab;
    }

    @Bean
    @Qualifier("ideTab")
    @Order(2)
    public Tab beanFilesTab(BeanFileBrowserPane beanFileBrowserPane) {
        final Tab tab = new Tab(s("Bean files"), beanFileBrowserPane);
        tab.setClosable(false);
        return tab;
    }
}
