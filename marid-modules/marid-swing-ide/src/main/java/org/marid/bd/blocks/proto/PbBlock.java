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

package org.marid.bd.blocks.proto;

import groovyjarjarasm.asm.Opcodes;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.marid.bd.BlockColors;
import org.marid.bd.ConfigurableBlock;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;
import org.marid.service.proto.pb.PbService;
import org.springframework.stereotype.Service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.util.EventListener;
import java.util.Objects;

import static org.codehaus.groovy.ast.ClassHelper.makeCached;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock(name = "Proto Builder", iconText = "PB", color = BlockColors.RED)
@XmlRootElement
public class PbBlock extends StandardBlock implements ConfigurableBlock {

    protected String className;
    protected String beanName;

    public final Out serviceOut = new Out("service", ClassNode.class, this::output);

    protected ClassNode output() {
        final ClassNode classNode = new ClassNode(className, Opcodes.ACC_PUBLIC, makeCached(PbService.class));
        final AnnotationNode serviceNode = new AnnotationNode(makeCached(Service.class));
        if (!beanName.isEmpty()) {
            serviceNode.addMember("value", new ConstantExpression(beanName));
        }
        classNode.addAnnotation(serviceNode);
        return classNode;
    }

    @XmlAttribute
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        if (!Objects.equals(className, this.className)) {
            this.className = className;
            fireEvent(PbBlockListener.class, l -> l.classNameChanged(className));
        }
    }

    @XmlAttribute
    public String getBeanName() {
        return beanName;
    }

    public void setBeanName(String beanName) {
        if (!Objects.equals(beanName, this.beanName)) {
            this.beanName = beanName;
            fireEvent(PbBlockListener.class, l -> l.beanNameChanged(beanName));
        }
    }

    @Override
    public String getLabel() {
        return beanName;
    }

    @Override
    public PbBlockEditor createWindow(Window parent) {
        return new PbBlockEditor(parent, this);
    }

    @Override
    public void reset() {
        className = "PBService";
        beanName = "pbService";
    }

    public interface PbBlockListener extends EventListener {

        void classNameChanged(String className);

        void beanNameChanged(String beanName);
    }
}
