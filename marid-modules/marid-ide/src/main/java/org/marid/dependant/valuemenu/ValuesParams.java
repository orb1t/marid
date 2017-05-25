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

package org.marid.dependant.valuemenu;

import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.WritableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import org.marid.dependant.beantree.items.DataTreeItem;
import org.marid.dependant.beantree.items.FileTreeItem;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.BeanFile;
import org.marid.spring.xml.DElement;
import org.springframework.core.ResolvableType;

/**
 * @author Dmitry Ovchinnikov
 */
public class ValuesParams {

    public final ProjectProfile profile;
    public final BeanFile file;
    public final WritableValue<DElement> element;
    public final ResolvableType type;
    public final ObservableStringValue name;
    public final ObservableList<MenuItem> menuItems;

    public ValuesParams(ProjectProfile profile,
                        BeanFile file,
                        WritableValue<DElement> element,
                        ResolvableType type,
                        ObservableStringValue name,
                        ObservableList<MenuItem> menuItems) {
        this.profile = profile;
        this.file = file;
        this.element = element;
        this.type = type;
        this.name = name;
        this.menuItems = menuItems;
    }

    public ValuesParams(ProjectProfile profile,
                        DataTreeItem<?> item,
                        ResolvableType type,
                        ObservableList<MenuItem> items) {
        this(profile, item.find(FileTreeItem.class).elem, item.elem.dataProperty(), type, item.elem.nameProperty(), items);
    }
}
