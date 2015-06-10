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
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.marid.bd.BlockColors;
import org.marid.bd.ClassNodeBuildTrigger;
import org.marid.bd.ConfigurableBlock;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;
import org.marid.groovy.MapProxies;
import org.marid.service.proto.pb.PbService;
import org.marid.service.proto.pb.PbServiceConfiguration;
import org.springframework.stereotype.Service;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.util.Arrays;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.singletonList;
import static org.codehaus.groovy.ast.ClassHelper.makeCached;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock(name = "Proto Service", iconText = "PB", color = BlockColors.RED)
@XmlRootElement
public class PbServiceBlock extends StandardBlock implements ConfigurableBlock, ClassNodeBuildTrigger {

    protected String className = "PBService";
    protected String beanName = "pbService";

    protected ClassNode classNode;

    protected MapEntryExpression[] buses;

    public final In busesInput = new In("buses", MapEntryExpression[].class, v -> buses = v);
    public final Out serviceOut = new Out("service", ClassNode.class, this::output);

    protected ClassNode output() {
        classNode = new ClassNode(className, Opcodes.ACC_PUBLIC, makeCached(PbService.class));
        final AnnotationNode serviceNode = new AnnotationNode(makeCached(Service.class));
        if (!beanName.isEmpty()) {
            serviceNode.addMember("value", new ConstantExpression(beanName));
        }
        classNode.addAnnotation(serviceNode);
        addConstructor();
        return classNode;
    }

    private void addConstructor() {
        final ClassExpression mapProxies = new ClassExpression(makeCached(MapProxies.class));
        final ArgumentListExpression proxyArgs = new ArgumentListExpression(
                new ClassExpression(makeCached(PbServiceConfiguration.class)),
                new MapExpression(singletonList(new MapEntryExpression(
                        new ConstantExpression("data"),
                        new MapExpression(singletonList(new MapEntryExpression(
                                new ConstantExpression("buses"),
                                new MapExpression(Arrays.asList(buses))
                        )))))
                )
        );
        final MethodCallExpression mapProxyCall = new MethodCallExpression(mapProxies, "newInstance", proxyArgs);
        final ConstructorCallExpression superCall = new ConstructorCallExpression(ClassNode.SUPER, new ArgumentListExpression(mapProxyCall));
        classNode.addConstructor(Opcodes.ACC_PUBLIC, new Parameter[0], new ClassNode[0], new ExpressionStatement(superCall));
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
    public PbServiceBlockEditor createWindow(Window parent) {
        return new PbServiceBlockEditor(parent, this);
    }

    @Override
    public void reset() {
        buses = new MapEntryExpression[0];
        classNode = null;
    }

    @Override
    public List<ClassNode> getClassNodes() {
        return singletonList(classNode);
    }

    public interface PbBlockListener extends EventListener {

        void classNameChanged(String className);

        void beanNameChanged(String beanName);
    }
}
