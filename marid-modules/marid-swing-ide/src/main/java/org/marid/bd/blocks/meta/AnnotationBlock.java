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

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.marid.bd.StandardBlock;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.marid.bd.blocks.expressions.NamedExpressionBlock.NamedExpression;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class AnnotationBlock extends StandardBlock {

    protected ClassNode classNode;
    protected NamedExpression[] members;

    protected final In<ClassNode> classNodeInput = new In<>("class", ClassNode.class, n -> classNode = n);
    protected final In<NamedExpression[]> membersInput = new In<>("parameters", NamedExpression[].class, v -> members = v);

    protected final Out<AnnotationNode> out = new Out<>("out", AnnotationNode.class, this::annotationNode);

    public AnnotationBlock() {
        super("Annotation Block", " @ ", "@", Color.CYAN.darker());
    }

    @Override
    public void reset() {
        classNode = ClassHelper.STRING_TYPE;
        members = new NamedExpression[0];
    }

    @Override
    public List<Input<?>> getInputs() {
        return Arrays.asList(classNodeInput, membersInput);
    }

    @Override
    public List<Output<?>> getOutputs() {
        return Collections.singletonList(out);
    }

    public AnnotationNode annotationNode() {
        final AnnotationNode node = new AnnotationNode(classNode);
        for (final NamedExpression member : members) {
            node.addMember(member.name, member.expression);
        }
        return node;
    }
}
