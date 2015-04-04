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

package org.marid.bd.blocks.meta;

import org.codehaus.groovy.ast.*;
import org.marid.bd.*;
import org.marid.bd.blocks.BdBlock;
import org.marid.bd.components.AbstractBlockComponentEditor;
import org.marid.swing.input.StringInputControl;

import javax.swing.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

import static groovyjarjarasm.asm.Opcodes.ACC_PUBLIC;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock(name = "User Class", label = "class", color = BlockColors.ANNOTATIONS_BLOCK_COLOR)
@XmlRootElement
@XmlSeeAlso({UserClassBlock.LinkedClassBlock.class})
public class UserClassBlock extends StandardBlock implements ConfigurableBlock, SingletonBlock {

    @XmlAttribute
    protected String className = "UserClass";

    protected ClassNode[] interfaces;
    protected MixinNode[] mixins;
    protected ClassNode superClass;
    protected MethodNode[] methods;
    protected FieldNode[] fields;
    protected AnnotationNode[] annotations;
    protected ConstructorNode[] constructors;

    public final In interfacesInput = new In("interfaces", ClassNode[].class, v -> interfaces = v);
    public final In mixinsInput = new In("mixins", MixinNode[].class, v -> mixins = v);
    public final In superClassInput = new In("super", ClassNode.class, v -> superClass = v);
    public final In methodsInput = new In("methods", MethodNode[].class, v -> methods = v);
    public final In fieldsInput = new In("fields", FieldNode[].class, v -> fields = v);
    public final In annotationsInput = new In("annotations", AnnotationNode[].class, v -> annotations = v);
    public final In constructorsInput = new In("constructors", ConstructorNode[].class, v -> constructors = v);
    public final Out export = new Out("class", ClassNode.class, this::classNode);

    @Override
    public void reset() {
        interfaces = new ClassNode[0];
        mixins = new MixinNode[0];
        superClass = ClassHelper.OBJECT_TYPE;
        methods = new MethodNode[0];
        fields = new FieldNode[0];
        annotations = new AnnotationNode[0];
        constructors = new ConstructorNode[0];
    }

    @Override
    public List<Out> getExports() {
        return Collections.singletonList(export);
    }

    @Override
    public String getLabel() {
        return className == null ? "UserClass" : className;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String name) {
        if (!Objects.equals(name, className)) {
            className = name;
            fireEvent(ClassNameListener.class, l -> l.onChange(name));
        }
    }

    protected ClassNode classNode() {
        final ClassNode classNode = new ClassNode(className, ACC_PUBLIC, superClass, interfaces, mixins);
        classNode.addAnnotations(Arrays.asList(annotations));
        Arrays.asList(fields).forEach(classNode::addField);
        Arrays.asList(methods).forEach(classNode::addMethod);
        Arrays.asList(constructors).forEach(classNode::addConstructor);
        return classNode;
    }

    @Override
    public Window createWindow(Window parent) {
        return new UserClassBlockConfigurer(parent);
    }

    @Override
    public Block blockPort() {
        return className == null ? null : new LinkedClassBlock(className);
    }

    private class UserClassBlockConfigurer extends AbstractBlockComponentEditor<UserClassBlock> {

        private final StringInputControl nameCtl = new StringInputControl();

        public UserClassBlockConfigurer(Window window) {
            super(window, UserClassBlock.this);
            if (className != null) {
                nameCtl.setInputValue(className);
            }
            tabPane("Common").addLine("Class name", nameCtl);
        }

        @Override
        protected void onSubmit(Action action, ActionEvent actionEvent) throws Exception {
            setClassName(nameCtl.getInputValue());
        }
    }

    interface ClassNameListener extends EventListener {

        void onChange(String className);
    }

    @XmlRootElement
    public static class LinkedClassBlock extends StandardBlock {

        @XmlAttribute
        private final String className;

        private final Out out;

        public LinkedClassBlock(String className) {
            this.className = className;
            this.out = new Out("class", ClassNode.class, () -> ClassHelper.make(className));
        }

        public LinkedClassBlock() {
            this(null);
        }

        @Override
        protected Color color() {
            return new Color(BlockColors.META_BLOCK_COLOR);
        }

        @Override
        public String getLabel() {
            return getClassName();
        }

        public String getClassName() {
            return className;
        }

        @Override
        public List<In> getInputs() {
            return Collections.emptyList();
        }

        @Override
        public List<Out> getOutputs() {
            return Collections.singletonList(out);
        }
    }
}
