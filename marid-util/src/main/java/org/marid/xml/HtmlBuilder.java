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

import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Collections;
import java.util.Map;

import static org.marid.misc.StringUtils.stringOrNull;

public class HtmlBuilder extends DomBuilder {

  private static final DocumentBuilder DOCUMENT_BUILDER;

  static {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
    try {
      DOCUMENT_BUILDER = documentBuilderFactory.newDocumentBuilder();
    } catch (Exception impossibleException) {
      throw new IllegalStateException(impossibleException);
    }
  }

  public HtmlBuilder(Map<String, ?> attributes) {
    super(DOCUMENT_BUILDER.newDocument().createElement("html"));
    getDocument().appendChild(getElement());
    attributes.forEach((k, v) -> getElement().setAttribute(k, stringOrNull(v)));
  }

  public HtmlBuilder() {
    this(Collections.emptyMap());
  }

  @Override
  public Node getNodeToTransform() {
    return getDocument();
  }
}
