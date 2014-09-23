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
import org.marid.itf.Named;

import java.beans.ConstructorProperties;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 */
public class Schema implements Named {

    protected String name;
    protected final List<Block> blocks;
    protected final List<BlockLink> links;

    @ConstructorProperties({"name", "blocks", "links"})
    public Schema(String name, List<Block> blocks, List<BlockLink> links) {
        this.name = name;
        this.blocks = blocks;
        this.links = links;
    }

    public Schema() {
        this(new UID().toString(), new ArrayList<>(), new ArrayList<>());
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<BlockLink> getLinks() {
        return links;
    }

    public void addBlockLink(BlockLink link) {
        if (links.stream().noneMatch(l -> l.matches(link.getBlockOutput(), link.getBlockInput()))) {
            if (links.add(link)) {
                // fire event
            }
        }
    }

    public void removeBlockLink(BlockLink link) {
        if (links.remove(link)) {
            // fire event
        }
    }
    
    public void addBlock(Block block) {
        if (blocks.add(block)) {
            // fire event
        }
    }

    public void removeBlock(Block block) {
        if (blocks.remove(block)) {
            // fire event
        }
    }

    @Override
    public String getName() {
        return name;
    }
}
