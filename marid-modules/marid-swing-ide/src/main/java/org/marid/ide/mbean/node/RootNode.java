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

package org.marid.ide.mbean.node;

import org.marid.jmx.DummyMBeanServerConnection;
import org.marid.swing.tree.TRootNode;

import javax.management.MBeanServerConnection;
import javax.swing.*;
import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author Dmitry Ovchinnikov.
 */
public class RootNode implements TRootNode<RootNode, BeanNode>, Node {

    protected final MBeanServerConnection server;
    protected final List<BeanNode> children;

    public RootNode(MBeanServerConnection server) {
        this.server = server == null ? DummyMBeanServerConnection.INSTANCE : server;
        try {
            children = this.server.queryMBeans(null, null).stream().map(v -> new BeanNode(this, v)).collect(toList());
        } catch (IOException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    public List<BeanNode> getChildren() {
        return children;
    }

    @Override
    public RootNode getRoot() {
        return this;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    @Override
    public String getName() {
        try {
            return server.getDefaultDomain();
        } catch (IOException x) {
            return server.toString();
        }
    }

    @Override
    public String getDescription() {
        return server.toString();
    }

    @Override
    public Class<?> getValueType() {
        return Void.class;
    }

    @Override
    public String getPath() {
        return "";
    }

    public MBeanServerConnection getServer() {
        return server;
    }
}
