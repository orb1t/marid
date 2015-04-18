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

import images.Images;
import org.marid.bd.blocks.BdBlock;
import org.marid.bd.components.BlockLabel;
import org.marid.bd.components.StandardBlockComponent;
import org.marid.dyn.MetaInfo;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class StandardBlock extends Block {

    protected final String name;
    protected final ImageIcon visualRepresentation;
    protected final String label;
    protected final Color color;

    public StandardBlock() {
        this.name = name();
        this.color = color();
        this.visualRepresentation = visualRepresentation();
        this.label = label();
    }

    protected String name() {
        final BdBlock block = getClass().getAnnotation(BdBlock.class);
        return block != null
                ? (block.name().isEmpty() ? getClass().getSimpleName() : block.name())
                : getClass().getSimpleName();
    }

    protected ImageIcon visualRepresentation() {
        final BdBlock block = getClass().getAnnotation(BdBlock.class);
        final String iconText = block != null ? (block.iconText().isEmpty() ? label() : block.iconText()) : label();
        return Images.getIconFromText(iconText, 32, 32, color, Color.WHITE);
    }

    protected String label() {
        final BdBlock block = getClass().getAnnotation(BdBlock.class);
        return block != null ? (block.label().isEmpty() ? name() : block.label()) : name();
    }

    protected Color color() {
        final BdBlock block = getClass().getAnnotation(BdBlock.class);
        if (block != null && block.color() >= 0) {
            return new Color(block.color());
        } else {
            final Package pkg = getClass().getPackage();
            if (pkg != null) {
                final MetaInfo metaInfo = pkg.getAnnotation(MetaInfo.class);
                if (metaInfo != null) {
                    return new Color(metaInfo.color());
                }
            }
            return Color.BLACK;
        }
    }

    @Override
    public StandardBlockComponent<? extends StandardBlock> createComponent() {
        return new StandardBlockComponent<>(this, c -> c.add(new BlockLabel(this::getLabel, this::getColor)));
    }

    @Override
    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }

    @Override
    public ImageIcon getVisualRepresentation() {
        return visualRepresentation;
    }
}
