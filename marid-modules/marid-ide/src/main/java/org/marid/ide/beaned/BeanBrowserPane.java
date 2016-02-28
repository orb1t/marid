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

import de.jensd.fx.glyphs.octicons.OctIcon;
import javafx.scene.layout.BorderPane;
import org.marid.jfx.toolbar.ToolbarBuilder;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;

import static org.marid.jfx.ScrollPanes.scrollPane;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanBrowserPane extends BorderPane implements LogSupport, L10nSupport {

    public BeanBrowserPane(BeanEditorPane editorPane) {
        final BeanBrowser beanBrowser = new BeanBrowser(editorPane);
        setCenter(scrollPane(beanBrowser));
        setTop(new ToolbarBuilder()
                .add("Add bean", OctIcon.FILE_DIRECTORY_CREATE,
                        event -> {},
                        b -> b.disableProperty().bind(beanBrowser.getSelectionModel().selectedItemProperty().isNull()))
                .build());
    }
}
