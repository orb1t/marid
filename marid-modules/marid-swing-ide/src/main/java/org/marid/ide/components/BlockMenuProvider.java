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

package org.marid.ide.components;

import org.marid.bd.Block;
import org.marid.bd.BlockGroups;
import org.marid.bd.blocks.BdBlock;
import org.marid.bd.common.DndMenuItem;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.util.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BlockMenuProvider implements L10nSupport, LogSupport {

    private final List<Block> blocks;

    @Autowired
    public BlockMenuProvider(@Lazy @BdBlock List<Block> blocks) {
        this.blocks = blocks;
    }

    public void fillMenu(JMenu menu) {
        final Map<String, Set<Block>> blockMap = new TreeMap<>();
        blocks.forEach(b -> {
            final BdBlock bdBlock = b.getClass().getAnnotation(BdBlock.class);
            final String g;
            if (bdBlock.group().isEmpty()) {
                if (b.getClass().getPackage() == null) {
                    g = "common";
                } else {
                    final int index = b.getClass().getPackage().getName().lastIndexOf('.');
                    g = b.getClass().getPackage().getName().substring(index + 1);
                }
            } else {
                g = bdBlock.group();
            }
            blockMap.computeIfAbsent(g, v -> new LinkedHashSet<>()).add(b);
        });
        blockMap.forEach((group, blockSet) -> {
            final BlockGroups.BlockGroup blockGroup = BlockGroups.blockGroup(group);
            final JMenu groupMenu = new JMenu(s(blockGroup.name));
            groupMenu.setIcon(blockGroup.icon);
            menu.add(groupMenu);
            blockSet.forEach(block -> {
                final ImageIcon imageIcon = block.getVisualRepresentation(22, 22);
                groupMenu.add(new DndMenuItem(imageIcon, block.getName(), () -> block));
            });
        });
    }
}
