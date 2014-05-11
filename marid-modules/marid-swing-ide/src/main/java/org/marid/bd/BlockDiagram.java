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

package org.marid.bd;

import org.marid.bd.blocks.PortBlock;

import javax.swing.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement(name = "block-diagram")
public class BlockDiagram extends Block {

    @XmlElementWrapper(name = "blocks")
    @XmlElementRef
    private final List<? extends Block> blocks = new ArrayList<>();

    @XmlAttribute
    private final String name;

    public BlockDiagram(String name) {
        this.name = name;
    }

    @Override
    public JComponent getComponent() {
        return null;
    }

    @Override
    public Window getEditor() {
        return new BlockDiagramFrame(new BlockDiagramEditor(this));
    }

    @Override
    public List<? extends Port> getPorts() {
        return blocks.stream().filter(b -> b instanceof PortBlock).<Port>map(b -> b.getPorts().get(0)).collect(toList());
    }

    @Override
    public String getName() {
        return name;
    }
}
