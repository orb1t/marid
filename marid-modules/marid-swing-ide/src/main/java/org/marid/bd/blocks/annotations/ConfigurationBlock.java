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

package org.marid.bd.blocks.annotations;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.marid.bd.BlockColors;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableMBeanExport;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock(name = "Configuration", label = "@Configuration", color = BlockColors.ANNOTATIONS_BLOCK_COLOR)
@XmlRootElement
public class ConfigurationBlock extends StandardBlock {

    public static final ClassNode CONFIGURATION_CLASS = ClassHelper.make(Configuration.class);
    public static final ClassNode ENABLE_MBEAN_EXPORT_CLASS = ClassHelper.make(EnableMBeanExport.class);

    public final Out out = new Out("node", AnnotationNode[].class, this::value);

    public AnnotationNode[] value() {
        return new AnnotationNode[] {
                new AnnotationNode(CONFIGURATION_CLASS),
                new AnnotationNode(ENABLE_MBEAN_EXPORT_CLASS)
        };
    }
}
