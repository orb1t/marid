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

package org.marid.bd.blocks;

import images.Images;
import org.marid.bd.Block;
import org.marid.swing.actions.GenericAction;

import javax.swing.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement(name = "port-block")
public class PortBlock extends Block {

    @XmlAttribute
    private String dataType;

    @XmlAttribute
    private String icon;

    @XmlElement
    private final PortKey portKey;

    private final List<? extends Port> ports = Collections.singletonList(new Port() {
        @Override
        public PortKey getPortKey() {
            return portKey;
        }

        @Override
        public Type getDataType() {
            return null;
        }

        @Override
        public ImageIcon getIcon() {
            return Images.getIcon(icon);
        }
    });

    private PortBlock() {
        this.portKey = null;
    }

    public PortBlock(PortKey portKey, String dataType, String icon) {
        this.portKey = portKey;
        this.dataType = dataType;
        this.icon = icon;
    }

    @Override
    public Window getEditor() {
        return new JWindow();
    }

    @Override
    public String getName() {
        return portKey.name;
    }

    @Override
    public JComponent getComponent() {
        final Supplier<Action> actionSupplier = () -> {
            final Port p = getPorts().get(0);
            return new GenericAction(portKey.name, p.getIcon(), ev -> {
                final JToggleButton button = (JToggleButton) ev.getSource();

            });
        };
        return new JToggleButton(actionSupplier.get()) {
            @Override
            public void validate() {
                setAction(actionSupplier.get());
                super.validate();
            }
        };
    }

    @Override
    public List<? extends Port> getPorts() {
        return ports;
    }
}
