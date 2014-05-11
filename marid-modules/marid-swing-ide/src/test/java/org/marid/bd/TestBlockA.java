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

import javax.swing.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov.
 */
@XmlRootElement(name = "test-block-a")
public class TestBlockA extends Block {

    @XmlAttribute
    final int q;

    public TestBlockA() {
        this(0);
    }

    public TestBlockA(int q) {
        this.q = q;
    }

    @Override
    public JComponent getComponent() {
        return new JPanel();
    }

    @Override
    public JFrame getEditor() {
        return new JFrame();
    }

    @Override
    public String getName() {
        return "blockA";
    }

    @Override
    public List<? extends Port> getPorts() {
        return Collections.emptyList();
    }
}
