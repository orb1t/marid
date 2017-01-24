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

package org.marid.dependant.beaneditor.mapeditor;

import org.marid.idefx.controls.CommonTableView;
import org.marid.spring.xml.DMap;
import org.marid.spring.xml.DMapEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class MapEditorTable extends CommonTableView<DMapEntry> {

    private final ResolvableType keyType;
    private final ResolvableType valueType;

    @Autowired
    public MapEditorTable(DMap map, ResolvableType keyType, ResolvableType valueType) {
        super(map.entries);
        this.keyType = keyType;
        this.valueType = valueType;
        setEditable(true);
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }
}
