/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid;

import org.junit.Test;
import org.marid.deploy.DeployDescriptor;
import org.marid.deploy.DeployDescriptor.JmxDescriptor;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class DeployDescriptorTest {

    @Test
    public void saveRestoreTest() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(DeployDescriptor.class);
        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        StringWriter sw = new StringWriter();
        DeployDescriptor descriptor = new DeployDescriptor()
                .addClassPath("a")
                .addClassPath("b")
                .setJmxDescriptor(new JmxDescriptor()
                        .setEnabled(true)
                        .setPort(10999));
        m.marshal(descriptor, sw);
        Unmarshaller u = ctx.createUnmarshaller();
        DeployDescriptor clone = (DeployDescriptor) u.unmarshal(new StringReader(sw.toString()));
        assertEquals(descriptor, clone);
    }
}
