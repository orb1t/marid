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
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.marid.bd.BlockColors;
import org.marid.bd.ConfigurableBlock;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;
import org.marid.bd.components.AbstractBlockComponentEditor;

import javax.swing.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.EventListener;
import java.util.Objects;

import static groovyjarjarasm.asm.Opcodes.ACC_PUBLIC;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock(name = "Method Block", label = "method", color = BlockColors.ANNOTATIONS_BLOCK_COLOR)
@XmlRootElement
public class MethodBlock extends StandardBlock implements ConfigurableBlock {

    @XmlAttribute
    protected String methodName = "method";

    protected ClassNode returnType;
    protected Parameter[] parameters;
    protected Statement body;
    protected AnnotationNode[] annotationNodes;

    public final In returnTypeInput = new In("returnType", ClassNode.class, v -> returnType = v);
    public final In parametersInput = new In("parameters", Parameter[].class, v -> parameters = v);
    public final In bodyInput = new In("body", Statement.class, true, v -> body = v);
    public final In annotationsInput = new In("annotations", AnnotationNode[].class, v -> annotationNodes = v);
    public final Out out = new Out("out", MethodNode.class, this::methodNode);

    public void setMethodName(String newMethodName) {
        if (!Objects.equals(newMethodName, methodName)) {
            methodName = newMethodName;
            fireEvent(MethodBlockListener.class, l -> l.methodNameChanged(newMethodName));
        }
    }

    public String getMethodName() {
        return methodName;
    }

    @Override
    public String getLabel() {
        return methodName;
    }

    public MethodNode methodNode() {
        final MethodNode n = new MethodNode(methodName, ACC_PUBLIC, returnType, parameters, new ClassNode[0], body);
        n.addAnnotations(Arrays.asList(annotationNodes));
        return n;
    }

    @Override
    public void reset() {
        returnType = ClassHelper.OBJECT_TYPE;
        parameters = new Parameter[0];
        body = EmptyStatement.INSTANCE;
        annotationNodes = new AnnotationNode[0];
    }

    @Override
    public Window createWindow(Window parent) {
        final JTextField methodNameField = new JTextField(methodName, 40);
        return new AbstractBlockComponentEditor<MethodBlock>(parent, this) {
            {
                tabPane("Common").addLine("Method name", methodNameField);
            }

            @Override
            protected void onSubmit(Action action, ActionEvent actionEvent) throws Exception {
                setMethodName(methodNameField.getText());
            }
        };
    }

    public interface MethodBlockListener extends EventListener {

        void methodNameChanged(String name);
    }
}
