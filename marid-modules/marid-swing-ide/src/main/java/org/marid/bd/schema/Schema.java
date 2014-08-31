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

import org.codehaus.groovy.ast.ClassNode;
import org.marid.bd.*;

import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 */
public class Schema extends AbstractBlock implements NamedBlock {

    protected final List<Block> blocks;
    protected final List<BlockLink> links;

    @ConstructorProperties({"blocks", "links"})
    public Schema(List<Block> blocks, List<BlockLink> links) {
        this.blocks = blocks;
        this.links = links;
    }

    public Schema() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<BlockLink> getLinks() {
        return links;
    }

    public void addBlockLink(BlockLink link) {
        if (links.stream().noneMatch(l -> l.matches(link.getOutput(), link.getInput()))) {
            if (links.add(link)) {
                fireEvent(SchemaListener.class, l -> l.addedLink(link));
            }
        }
    }

    public void removeBlockLink(BlockLink link) {
        if (links.remove(link)) {
            fireEvent(SchemaListener.class, l -> l.removedLink(link));
        }
    }
    
    public void addBlock(Block block) {
        if (blocks.add(block)) {
            fireEvent(SchemaListener.class, l -> l.addedBlock(block));
        }
    }

    public void removeBlock(Block block) {
        if (blocks.remove(block)) {
            fireEvent(SchemaListener.class, l -> l.removedBlock(block));
        }
    }

    @Override
    public BlockComponent createComponent() {
        return null;
    }

    @Override
    public void reset() {

    }

    @Override
    public List<Input<?>> getInputs() {
        return Collections.emptyList();
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.emptyList();
    }

    public ClassNode toClassNode() {
        return null;
    }
}
