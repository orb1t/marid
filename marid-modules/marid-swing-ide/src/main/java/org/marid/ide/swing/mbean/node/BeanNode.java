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
import org.marid.swing.tree.TNode;

import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 */
public class BeanNode implements TNode<BeanNode, RootNode, Group<?, ?>>, Node {

    protected static final ImageIcon ICON = Images.getIcon("bean.png");

    protected final RootNode parent;
    protected final ObjectInstance instance;
    protected final List<Group<?, ?>> children = new ArrayList<>();

    public BeanNode(RootNode parent, ObjectInstance instance) {
        this.parent = parent;
        this.instance = instance;
        children.add(new AttributeGroupNode(this));
    }

    @Override
    public RootNode getParent() {
        return parent;
    }

    @Override
    public List<Group<?, ?>> getChildren() {
        return children;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public ImageIcon getIcon() {
        return ICON;
    }

    @Override
    public String getName() {
        return instance.getObjectName().toString();
    }

    @Override
    public String getDescription() {
        return instance.toString();
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
        return parent;
    }

    public ObjectInstance getInstance() {
        return instance;
    }

    public ObjectName getObjectName() {
        return instance.getObjectName();
    }
}
