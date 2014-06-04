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
import org.marid.bd.BlockComponent;
import org.marid.bd.BlockLink;
import org.marid.bd.NamedBlock;

import java.awt.*;
import java.beans.ConstructorProperties;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 */
public class Schema extends NamedBlock {

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

    public void addBlockLink(Block.Output output, Block.Input input) {
        if (links.stream().noneMatch(l -> l.matches(output, input))) {
            final BlockLink blockLink = new BlockLink(output, input);
            if (links.add(blockLink)) {
                fireEvent(AddBlockLinkListener.class, new AddBlockLinkEvent(blockLink));
            }
        }
    }

    public void removeBlockLink(BlockLink link) {
        if (links.remove(link)) {
            fireEvent(RemoveBlockLinkListener.class, new RemoveBlockLinkEvent(link));
        }
    }
    
    public void addBlock(Block block) {
        if (blocks.add(block)) {
            fireEvent(AddBlockListener.class, new AddBlockEvent(block));
        }
    }

    public void removeBlock(Block block) {
        if (blocks.remove(block)) {
            fireEvent(RemoveBlockListener.class, new RemoveBlockEvent(block));
        }
    }

    @Override
    public BlockComponent createComponent() {
        return null;
    }

    @Override
    public Window createWindow() {
        return null;
    }

    @Override
    public List<Input<?>> getInputs() {
        return Collections.emptyList();
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.emptyList();
    }

    public class AddBlockEvent extends BlockEvent {

        public final Block block;

        public AddBlockEvent(Block block) {
            this.block = block;
        }

        @Override
        public Schema getSource() {
            return (Schema) super.getSource();
        }
    }

    public class RemoveBlockEvent extends BlockEvent {

        public final Block block;

        public RemoveBlockEvent(Block block) {
            this.block = block;
        }

        @Override
        public Schema getSource() {
            return (Schema) super.getSource();
        }
    }

    public class AddBlockLinkEvent extends BlockEvent {

        public final BlockLink link;

        public AddBlockLinkEvent(BlockLink link) {
            this.link = link;
        }

        @Override
        public Schema getSource() {
            return (Schema) super.getSource();
        }
    }

    public class RemoveBlockLinkEvent extends BlockEvent {

        public final BlockLink link;

        public RemoveBlockLinkEvent(BlockLink link) {
            this.link = link;
        }

        @Override
        public Schema getSource() {
            return (Schema) super.getSource();
        }
    }

    public interface AddBlockListener extends BlockEventListener<AddBlockEvent> {
    }

    public interface RemoveBlockListener extends BlockEventListener<RemoveBlockEvent> {
    }

    public interface AddBlockLinkListener extends BlockEventListener<AddBlockLinkEvent> {
    }

    public interface RemoveBlockLinkListener extends BlockEventListener<RemoveBlockLinkEvent> {
    }
}
