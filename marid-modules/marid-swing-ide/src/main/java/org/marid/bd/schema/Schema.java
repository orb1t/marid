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
import org.marid.bd.Block;
import org.marid.bd.BlockLink;
import org.marid.itf.Named;

import javax.xml.bind.annotation.*;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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

    public List<ClassNode> toCode() {
        getBlocks().forEach(Block::reset);
        final Predicate<Block> filter = b -> b.getOutputs().isEmpty() && !b.getExports().isEmpty();
        final Set<Block> blocks = getBlocks().stream().filter(b -> !filter.test(b)).collect(toSet());
        final Set<Block> terminalBlocks = getBlocks().stream().filter(filter).collect(toSet());
        terminalBlocks.forEach(b -> pass(blocks, b));
        return terminalBlocks.stream()
                .flatMap(b -> b.getExports().stream().filter(o -> o.getOutputType() == ClassNode.class))
                .map(o -> (ClassNode) o.get())
                .collect(toList());
    }

    void pass(Set<Block> blocks, Block block) {
        final Set<BlockLink> links = getLinks().stream().filter(l -> l.getTarget() == block).collect(toSet());
        final Set<Block> sources = links.stream().map(BlockLink::getSource).collect(toSet());
        final Set<Block> rest = blocks.stream().filter(b -> !sources.contains(b)).collect(toSet());
        sources.forEach(b -> pass(rest, b));
        block.transfer(links);
    }
}
