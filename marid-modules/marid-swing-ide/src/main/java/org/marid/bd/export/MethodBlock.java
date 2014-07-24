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

package org.marid.bd.export;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.marid.bd.StandardBlock;
import org.marid.bd.components.AbstractBlockComponentEditor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

import static groovyjarjarasm.asm.Opcodes.ACC_PUBLIC;

/**
 * @author Dmitry Ovchinnikov
 */
public class MethodBlock extends StandardBlock {

    protected String methodName = "method";
    protected ClassNode returnType;
    protected Parameter[] parameters;
    protected Statement body;
    protected AnnotationNode[] annotationNodes;

    protected final In<ClassNode> returnTypeInput = new In<>("returnType", ClassNode.class, v -> returnType = v);
    protected final In<Parameter[]> parametersInput = new In<>("parameters", Parameter[].class, v -> parameters = v);
    protected final In<Statement> bodyInput = new In<>("body", Statement.class, v -> body = v);
    protected final In<AnnotationNode[]> annotationsInput = new In<>("annotations", AnnotationNode[].class, v -> annotationNodes = v);

    protected final Out<MethodNode> out = new Out<>("out", MethodNode.class, this::methodNode);

    public MethodBlock() {
        super("Method Block", "method", "method", Color.CYAN.darker());
    }

    public void setMethodName(String newMethodName) {
        fire(MethodBlockListener.class, () -> methodName, m -> methodName = m, newMethodName, MethodBlockListener::methodNameChanged);
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
    public List<Input<?>> getInputs() {
        return Arrays.asList(returnTypeInput, parametersInput, bodyInput, annotationsInput);
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
                afterInit();
            }

            @Override
            protected void onSubmit(Action action, ActionEvent actionEvent) throws Exception {
                setMethodName(methodNameField.getText());
            }
        };
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(out);
    }

    public interface MethodBlockListener extends EventListener {

        void methodNameChanged(String oldName, String newName);
    }
}
