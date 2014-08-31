/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.ide.swing.context;

import org.marid.bd.schema.SchemaFrame;
import org.marid.ide.bde.BdeWindow;
import org.marid.ide.components.BlockMenuProvider;
import org.marid.logging.LogSupport;
import org.marid.swing.MaridAction;
import org.marid.swing.menu.ActionTreeElement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class MenuContext implements LogSupport {

    @Autowired
    BlockMenuProvider blockMenuProvider;

    @Bean
    public ActionTreeElement ideMenuActionTreeElement() {
        return new ActionTreeElement()
                .add("Services", null, servicesMenu -> {
                    servicesMenu
                            .add("Service configurer", null, new MaridAction("BDE Window", null, e -> {
                                new BdeWindow().setVisible(true);
                            }))
                            .add("Schema frame", null, new MaridAction("Schema frame", null, e -> {
                                new SchemaFrame(blockMenuProvider).setVisible(true);
                            }));
                });
    }
}
