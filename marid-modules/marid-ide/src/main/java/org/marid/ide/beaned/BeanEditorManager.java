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

package org.marid.ide.beaned;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.ide.toolbar.IdeToolbarItem;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Provider;

/**
 * @author Dmitry Ovchinnikov
 */
@ApplicationScoped
public class BeanEditorManager {

    @Produces
    @IdeMenuItem(menu = "File", text = "New", group = "fileNew", oIcons = {OctIcon.FILE_DIRECTORY_CREATE})
    @IdeToolbarItem(group = "file")
    public EventHandler<ActionEvent> projectSetup(Provider<BeanEditor> beanEditorProvider) {
        return event -> {
            final BeanEditor beanEditor = beanEditorProvider.get();
            beanEditor.show();
        };
    }

    @Produces
    @IdeMenuItem(menu = "File", text = "Open...", group = "fileOpen", mdIcons = {MaterialDesignIcon.OPEN_IN_NEW})
    @IdeToolbarItem(group = "file")
    public EventHandler<ActionEvent> projectSave() {
        return event -> {
        };
    }
}