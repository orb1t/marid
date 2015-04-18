/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.bd.common;

import images.Images;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.marid.bd.StandardBlock;

import javax.swing.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement
public class ClassLinkBlock extends StandardBlock {

    public final Out output;

    @XmlAttribute
    public final String className;

    public ClassLinkBlock(String className) {
        this.className = className;
        this.output = new Out("class", ClassNode.class, () -> ClassHelper.make(className));
    }

    private ClassLinkBlock() {
        this(null);
    }

    @Override
    public String getName() {
        return className;
    }

    public static ImageIcon getMenuIcon() {
        return Images.getIcon("link.png", 16);
    }

    @Override
    public Color getColor() {
        return Color.PINK;
    }

    @Override
    public String getLabel() {
        return className;
    }
}
