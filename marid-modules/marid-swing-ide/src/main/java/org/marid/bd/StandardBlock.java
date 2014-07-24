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
import org.marid.bd.components.BlockLabel;
import org.marid.bd.components.StandardBlockComponent;

import javax.swing.*;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class StandardBlock extends AbstractBlock {

    protected final String name;
    protected final ImageIcon visualRepresentation;
    protected final String label;
    protected final Color color;

    public StandardBlock(String name, String iconText, String label, Color color) {
        this.name = name;
        this.visualRepresentation = Images.getIconFromText(iconText, 32, 32, color, Color.WHITE);
        this.label = label;
        this.color = color;
    }

    @Override
    public BlockComponent createComponent() {
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
