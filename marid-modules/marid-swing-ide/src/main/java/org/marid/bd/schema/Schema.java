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

import javax.xml.bind.annotation.*;
import java.rmi.server.UID;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.marid.util.CollectionUtils.getArrayFunction;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement
@XmlSeeAlso({Block.class, BlockLink.class})
public class Schema implements Named {

    @XmlAttribute
    protected String name;

    @XmlElementWrapper(name = "blocks")
    @XmlElementRef
    protected final List<Block> blocks;

    @XmlElementWrapper(name = "links")
    @XmlElementRef
    protected final List<BlockLink> links;

    public Schema() {
        this(new UID().toString(), new ArrayList<>(), new ArrayList<>());
    }

    public Schema(String name, List<Block> blocks, List<BlockLink> links) {
        this.name = name;
        this.blocks = blocks;
        this.links = links;
    }

    public List<Block> getBlocks() {
        return blocks;
    }

    public List<BlockLink> getLinks() {
        return links;
    }

    public void addBlockLink(BlockLink link) {
        if (links.stream().noneMatch(l -> l.matches(link.getBlockOutput(), link.getBlockInput()))) {
            links.add(link);
        }
    }

    public void removeBlockLink(BlockLink link) {
        links.remove(link);
    }
    
    public void addBlock(Block block) {
        blocks.add(block);
    }

    public void removeBlock(Block block) {
        blocks.remove(block);
    }

    @Override
    public String getName() {
        return name;
    }

    public void build() {
        final Set<Block> blocks = Collections.newSetFromMap(new IdentityHashMap<>(getBlocks().size()));
        blocks.addAll(getBlocks());
        final Set<Block> passed = Collections.newSetFromMap(new IdentityHashMap<>(blocks.size()));
        blocks.stream()
                .filter(b -> b.getOutputs().isEmpty() || getLinks().stream().noneMatch(l -> l.getSource() == b))
                .forEach(b -> build(passed, b));
        blocks.removeAll(passed);
        blocks.forEach(b -> b.getOutputs().forEach(Block.Out::get));
        passed.forEach(Block::afterBuild);
        blocks.forEach(Block::afterBuild);
    }

    void build(Set<Block> blocks, Block block) {
        if (!blocks.add(block)) {
            return;
        }
        final List<Block> linked = getBlocks().stream()
                .filter(b -> getLinks().stream().anyMatch(l -> l.getSource() == b && l.getTarget() == block))
                .collect(toList());
        linked.forEach(b -> build(blocks, b));
        block.getInputs().forEach(i -> {
            final List<BlockLink> links = getLinks().stream().filter(l -> l.getBlockInput() == i).collect(toList());
            if (links.isEmpty()) {
                return;
            }
            if (i.getInputType().isArray()) {
                final Class<?> elementType = i.getInputType().getComponentType();
                i.set(links.stream().map(l -> l.getBlockOutput().get()).toArray(getArrayFunction(elementType)));
            } else {
                i.set(links.iterator().next().getBlockOutput().get());
            }
        });
        block.getOutputs().forEach(Block.Out::get);
    }
}
