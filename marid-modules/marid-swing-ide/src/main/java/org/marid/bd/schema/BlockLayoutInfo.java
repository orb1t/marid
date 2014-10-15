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

package org.marid.bd.schema;

import org.marid.bd.Block;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.awt.*;

/**
* @author Dmitry Ovchinnikov
*/
@XmlRootElement
@XmlSeeAlso({Block.class})
public class BlockLayoutInfo {

    @XmlAttribute
    private final int x;

    @XmlAttribute
    private final int y;

    @XmlAttribute
    @XmlIDREF
    private final Block block;

    public BlockLayoutInfo(Point point, Block block) {
        this.x = point.x;
        this.y = point.y;
        this.block = block;
    }

    public BlockLayoutInfo() {
        this(new Point(), null);
    }

    public Point getLocation() {
        return new Point(x, y);
    }

    public Block getBlock() {
        return block;
    }
}
