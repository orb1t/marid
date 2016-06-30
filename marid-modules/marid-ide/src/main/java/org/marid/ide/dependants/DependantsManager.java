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

package org.marid.ide.dependants;

import org.marid.dependant.iconviewer.IconViewerConfiguration;
import org.marid.jfx.action.FxAction;
import org.marid.spring.action.MenuAction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.marid.IdeDependants.startDependant;
import static org.marid.jfx.icons.FontIcon.M_OPEN_IN_BROWSER;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class DependantsManager {

    @Bean
    @MenuAction
    public FxAction iconViewerAction() {
        return new FxAction(null, "icons", "Tools")
                .setIcon(M_OPEN_IN_BROWSER)
                .setText("Icon viewer")
                .setEventHandler(event -> startDependant("iconViewer", IconViewerConfiguration.class));
    }
}
