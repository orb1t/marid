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

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.bd.constant.ConstantBlock;
import org.marid.bd.constant.ConstantBlockComponent;
import org.marid.bd.schema.SchemaModel;
import org.marid.test.NormalTests;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.marid.beans.MaridBeans.read;
import static org.marid.beans.MaridBeans.write;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class SchemaModelTest {

    @Test
    public void testPersistence() throws Exception {
        final SchemaModel model = new SchemaModel();
        final ConstantBlock constantBlock = new ConstantBlock();
        constantBlock.setValue(ConstantExpression.class.getCanonicalName() + ".PRIM_FALSE");
        final ConstantBlockComponent constantBlockComponent = constantBlock.createComponent();
        model.addBlock(constantBlockComponent, new Point(10, 20));
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        write(bos, model);
        final SchemaModel cloned = read(SchemaModel.class, new ByteArrayInputStream(bos.toByteArray()));
        assertEquals(1, cloned.getSchema().getBlocks().size());
        assertTrue(cloned.getSchema().getBlocks().get(0) instanceof ConstantBlock);
        final ConstantBlock clonedBlock = (ConstantBlock) cloned.getSchema().getBlocks().get(0);
        assertTrue(((ConstantExpression) clonedBlock.getOutputs().get(0).get()).isFalseExpression());
    }
}
