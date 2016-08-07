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

package org.marid.ide.configurations;

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.marid.ide.panes.filebrowser.BeanFileBrowserPane;
import org.marid.l10n.L10n;
import org.marid.spring.annotation.Q;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

import static javafx.scene.control.TabPane.TabClosingPolicy.ALL_TABS;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class TabConfiguration {

    @Bean
    @Q(TabConfiguration.class)
    @Order(1)
    public Tab beanFilesTab(BeanFileBrowserPane beanFileBrowserPane) {
        final Tab tab = new Tab(L10n.s("Bean files"), beanFileBrowserPane);
        tab.setClosable(false);
        tab.tabPaneProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.getSelectionModel().select(tab);
            }
        });
        return tab;
    }

    @Bean
    public TabPane ideTabPane(@Q(TabConfiguration.class) List<Tab> tabs) {
        final TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(ALL_TABS);
        tabs.forEach(tabPane.getTabs()::add);
        return tabPane;
    }
}
