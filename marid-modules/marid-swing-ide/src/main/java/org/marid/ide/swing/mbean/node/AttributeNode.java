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

package org.marid.ide.swing.mbean.node;

import images.Images;
import org.marid.swing.tree.TTerminalNode;

import javax.management.MBeanAttributeInfo;
import javax.swing.*;

/**
 * @author Dmitry Ovchinnikov.
 */
public class AttributeNode implements TTerminalNode<AttributeNode, AttributeGroupNode>, Node {

    protected static final ImageIcon ICON = Images.getIcon("attribute.png");

    protected final AttributeGroupNode parent;
    protected final MBeanAttributeInfo attributeInfo;

    public AttributeNode(AttributeGroupNode parent, MBeanAttributeInfo attributeInfo) {
        this.parent = parent;
        this.attributeInfo = attributeInfo;
    }

    @Override
    public AttributeGroupNode getParent() {
        return parent;
    }

    @Override
    public ImageIcon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return attributeInfo.getName();
    }

    @Override
    public String getDescription() {
        return attributeInfo.getDescription();
    }

    @Override
    public Class<?> getValueType() {
        return Void.class;
    }

    @Override
    public String getPath() {
        return getParent().getPath() + "/" + getName();
    }

    @Override
    public RootNode getRoot() {
        return getParent().getRoot();
    }

    @Override
    public String toString() {
        return getName();
    }
}
