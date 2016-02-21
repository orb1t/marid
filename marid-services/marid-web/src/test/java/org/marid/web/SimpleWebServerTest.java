/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.web;

import com.google.common.io.ByteStreams;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.marid.logging.LogSupport;
import org.marid.test.NormalTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

/**
 * @author Dmitry Ovchinnikov.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@Category({NormalTests.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class})
@ContextConfiguration(classes = {SimpleWebServerTestConf.class})
public class SimpleWebServerTest implements LogSupport {

    @Autowired
    private SimpleWebServer simpleWebServer;

    @Test
    public void testRequest() throws Exception {
        final URL url = new URL("http", "localhost", simpleWebServer.getPort(), "/");
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setUseCaches(false);
            connection.connect();
            assertEquals(HTTP_OK, connection.getResponseCode());
            try (final InputStream is = connection.getInputStream()) {
                final String response = new String(ByteStreams.toByteArray(is), UTF_8);
                assertEquals("Hello", response);
            }
        } finally {
            connection.disconnect();
        }
    }
}
