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

import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.swing.*;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Dmitry Ovchinnikov.
 */
public class AttributeGroupNode extends Group<AttributeGroupNode, AttributeNode> {

    protected static final ImageIcon ICON = Images.getIcon("attributes.png");

    protected final List<AttributeNode> children;

    public AttributeGroupNode(BeanNode parent) {
        super(parent, "Attributes");
        try {
            final MBeanServerConnection server = getRoot().server;
            final ObjectInstance instance = getParent().instance;
            final MBeanInfo beanInfo = server.getMBeanInfo(instance.getObjectName());
            children = Arrays.stream(beanInfo.getAttributes()).map(a -> new AttributeNode(this, a)).collect(toList());
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public List<AttributeNode> getChildren() {
        return children;
    }

    @Override
    public RootNode getRoot() {
        return getParent().getRoot();
    }

    @Override
    public ImageIcon getIcon() {
        return ICON;
    }

    @Override
    public String getDescription() {
        return s("Bean attributes");
    }

    @Override
    public Class<?> getValueType() {
        return Void.class;
    }

    @Override
    public String getPath() {
        return getParent().getPath() + "/" + getName();
    }
}
