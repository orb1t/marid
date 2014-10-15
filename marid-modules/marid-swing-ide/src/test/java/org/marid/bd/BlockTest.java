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

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.bd.blocks.annotations.BeanBlock;
import org.marid.bd.schema.SchemaModel;
import org.marid.test.NormalTests;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
public class BlockTest {

    @Test
    public void testConstantBlock() throws Exception {
        final BeanBlock block = new BeanBlock();
        final JAXBContext context = JAXBContext.newInstance(BeanBlock.class, SchemaModel.class);
        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        marshaller.marshal(block, bos);
        System.out.println(bos.toString());
    }
}
