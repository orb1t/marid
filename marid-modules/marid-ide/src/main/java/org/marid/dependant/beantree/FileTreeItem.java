/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.dependant.beantree;

import javafx.beans.binding.Binding;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import org.marid.spring.xml.BeanFile;

import static javafx.beans.binding.Bindings.createObjectBinding;
import static javafx.beans.binding.Bindings.createStringBinding;
import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcon.F_FILE;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
public class FileTreeItem extends AbstractTreeItem<BeanFile> {

    private final Binding<String> name;
    private final Binding<Node> icon;
    private final ObservableValue<String> type;
    private final ObservableValue<String> text;

    public FileTreeItem(BeanFile value) {
        super(value);
        name = createStringBinding(() -> value.path.get(value.path.size() - 1), value.observables());
        icon = createObjectBinding(() -> glyphIcon(F_FILE, 20));
        text = createStringBinding(value::getFilePath, value.observables());
        type = ls("file");
    }

    @Override
    public ObservableValue<String> name() {
        return name;
    }

    @Override
    public ObservableValue<Node> icon() {
        return icon;
    }

    @Override
    public ObservableValue<String> type() {
        return type;
    }

    @Override
    public ObservableValue<String> text() {
        return text;
    }

    @Override
    public ObservableValue<ContextMenu> menu() {
        return null;
    }
}
