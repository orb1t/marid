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

import java.util.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SchemaToCode {

    public static List<ClassNode> schemaToCode(Schema schema) {
        final List<ClassNode> classNodes = new ArrayList<>();
        final Set<Block> blocks = new HashSet<>(schema.getBlocks()), passed = new HashSet<>();
        final Set<BlockLink> links = new HashSet<>(schema.getLinks());
        final List<Set<Block>> layers = new LinkedList<>();
        while (!blocks.isEmpty()) {
            final Set<Block> set = new HashSet<>();
            for (final Iterator<Block> i = blocks.iterator(); i.hasNext(); ) {
                final Block block = i.next();
                if (block.getInputs().isEmpty()) {
                    i.remove();
                    set.add(block);
                    passed.add(block);
                } else {
                    final Set<BlockLink> linked = new HashSet<>();
                    for (final BlockLink link : links) {
                        if (link.getTarget().equals(block) && passed.contains(link.getSource())) {
                            linked.add(link);
                        }
                    }
                    if (linked.stream().allMatch(l -> passed.contains(l.getSource()))) {
                        i.remove();
                        set.add(block);
                        passed.add(block);
                    }
                }
            }
            layers.add(set);
        }
        for (final Set<Block> blockSet : layers) {
            for (final Block block : blockSet) {
                block.reset();
                links.stream().filter(l -> l.getTarget().equals(block)).forEach(BlockLink::transferValue);
                for (final Block.Output output : block.getExports()) {
                    final Object out = output.get();
                    if (out instanceof ClassNode) {
                        final ClassNode classNode = (ClassNode) out;
                        if (classNode.isPrimaryClassNode()) {
                            classNodes.add(classNode);
                        }
                    }
                }
            }
        }
        return classNodes;
    }
}
