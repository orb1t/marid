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
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.marid.io.JaxbStreams;
import org.marid.wrapper.JaxbDataTestSuite.AuthResponseTest;
import org.marid.wrapper.JaxbDataTestSuite.ClientDataTest;

import javax.xml.bind.JAXBContext;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov
 */
@RunWith(Suite.class)
@SuiteClasses({AuthResponseTest.class, ClientDataTest.class})
public class JaxbDataTestSuite {

    public static class DataTest<T> {

        private final JAXBContext context;
        private final T data;

        public DataTest(JAXBContext context, T data) {
            this.context = context;
            this.data = data;
        }

        @Test
        public void testReadWrite() throws Exception {
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            final DataOutputStream dos = new DataOutputStream(bos);
            JaxbStreams.write(context, dos, data);
            final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            final DataInputStream dis = new DataInputStream(bis);
            final Object restored = JaxbStreams.read(context, data.getClass(), dis);
            assertEquals(data, restored);
        }
    }

    public static class AuthResponseTest extends DataTest<AuthResponse> {

        public AuthResponseTest() {
            super(Session.JAXB_CONTEXT, new AuthResponse("ok", "guest", "uploader"));
        }
    }

    public static class ClientDataTest extends DataTest<ClientData> {

        public ClientDataTest() {
            super(Session.JAXB_CONTEXT, new ClientData()
                    .setJavaVersion("1.8")
                    .setPassword("abc")
                    .setUser("user"));
        }
    }
}
