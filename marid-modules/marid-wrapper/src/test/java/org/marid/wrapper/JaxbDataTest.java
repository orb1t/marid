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

package org.marid.wrapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.marid.io.JaxbStreams;

import javax.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@RunWith(Parameterized.class)
public class JaxbDataTest {

    private final JAXBContext context;
    private final Object object;

    public JaxbDataTest(JAXBContext context, Object object) {
        this.context = context;
        this.object = object;
    }

    @Parameters
    public static Collection<Object[]> parameters() {
        return Arrays.asList(
                new Object[] {Session.JAXB_CONTEXT, new AuthResponse("ok", "guest", "uploader")},
                new Object[] {Session.JAXB_CONTEXT, new ClientData()
                        .setJavaVersion("1.8")
                        .setPassword("abc")
                        .setUser("user")});
    }

    @Test
    public void test() throws Exception {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);
        JaxbStreams.write(context, dos, object);
        final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        final DataInputStream dis = new DataInputStream(bis);
        final Object restored = JaxbStreams.read(context, object.getClass(), dis);
        assertEquals(object, restored);
    }

    @Override
    public String toString() {
        return object.getClass().getSimpleName();
    }
}
