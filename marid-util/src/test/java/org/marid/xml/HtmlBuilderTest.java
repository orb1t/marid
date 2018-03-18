/*-
 * #%L
 * marid-util
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

package org.marid.xml;

import org.testng.annotations.Test;

import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static org.marid.test.TestGroups.NORMAL;
import static org.testng.Assert.assertEquals;

public class HtmlBuilderTest {

  @Test(groups = {NORMAL})
  public void testSimple() throws IOException {
    final StringWriter writer = new StringWriter();

    new HtmlBuilder()
        .e("head", e -> e.e("link", Map.of("rel", "xxx")))
        .e("body", e -> e.e("details"))
        .write(new StreamResult(writer));

    assertEquals(
        writer.toString().replaceAll("\\r\\n|\\r", "\n").trim(),
        "<!DOCTYPE html SYSTEM \"about:legacy-compat\">\n" +
            "<html>\n" +
            "  <head>\n" +
            "    <META http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\n" +
            "    <link rel=\"xxx\">\n" +
            "  </head>\n" +
            "  <body>\n" +
            "    <details></details>\n" +
            "  </body>\n" +
            "</html>");
  }
}
