/*-
 * #%L
 * marid-webapp
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
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

package org.marid.app.logging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;

import static java.nio.charset.StandardCharsets.UTF_8;

public class MaridLogging {

  public static void initLogging() throws Exception {
    System.setProperty("java.util.logging.manager", MaridLogManager.class.getName());

    final Map<String, String> properties = new LinkedHashMap<>();
    properties.put("handlers", "");
    properties.put(".level", Level.INFO.getName());

    final ByteArrayOutputStream os = new ByteArrayOutputStream();

    properties.forEach((k, v) -> {
      final byte[] line = (k + "=" + v + "\n").getBytes(UTF_8);
      os.write(line, 0, line.length);
    });

    LogManager.getLogManager().readConfiguration(new ByteArrayInputStream(os.toByteArray()));
    LogManager.getLogManager().getLogger("").addHandler(new MaridLogHandler());
  }
}
