/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.runtime;


import org.marid.runtime.beans.Bean;
import org.marid.runtime.context.MaridContext;
import org.marid.runtime.context.MaridRuntimeUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.marid.io.Xmls.read;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridLauncher {

    public static void main(String... args) throws Exception {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final URL beansXmlUrl = classLoader.getResource("META-INF/marid/beans.xml");
        if (beansXmlUrl == null) {
            throw new IllegalStateException("No beans.xml file found");
        }

        final AtomicReference<MaridContext> contextRef = new AtomicReference<>();
        MaridRuntimeUtils.daemonThread(contextRef).start();

        try (final Reader reader = new InputStreamReader(beansXmlUrl.openStream(), UTF_8)) {
            contextRef.set(new MaridContext(read(reader, Bean::new), classLoader, System.getProperties()));
        } catch (Throwable x) {
            x.printStackTrace();
        }
    }
}
