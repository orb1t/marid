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
import org.marid.bd.BlockLink;
import org.marid.spring.annotation.PrototypeComponent;
import org.marid.xml.XmlBindable;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement
@XmlSeeAlso({Schema.class, BlockLayoutInfo.class, BlockLinkLayoutInfo.class})
@XmlBindable
@PrototypeComponent
public class SchemaModel {

    @XmlElement
    protected final Schema schema;

    @XmlElementWrapper(name = "blocks")
    @XmlElementRef
    protected final List<BlockLayoutInfo> blocks;

    @XmlElementWrapper(name = "links")
    @XmlElementRef
    protected final List<BlockLinkLayoutInfo> links;

    public SchemaModel() {
        schema = new Schema();
        blocks = new ArrayList<>();
        links = new ArrayList<>();
    }

    public SchemaModel(SchemaEditor schemaEditor) {
        final List<Block> blockList = new ArrayList<>();
        final List<BlockLink> linkList = new ArrayList<>();
        this.blocks = new ArrayList<>();
        this.links = new ArrayList<>();
        schemaEditor.visitBlockComponents(c -> {
            blocks.add(new BlockLayoutInfo(c.getLocation(), c.getBlock()));
            blockList.add(c.getBlock());
        });
        schemaEditor.getLinkShapes().forEach(linkShape -> {
            final BlockLink link = new BlockLink(linkShape.output.getOutput(), linkShape.input.getInput());
            links.add(new BlockLinkLayoutInfo(link));
            linkList.add(link);
        });
        this.schema = new Schema(schemaEditor.getName(), blockList, linkList);
    }

    public Schema getSchema() {
        return schema;
    }

    public List<BlockLayoutInfo> getBlocks() {
        return blocks;
    }

    public List<BlockLinkLayoutInfo> getLinks() {
        return links;
    }
}
