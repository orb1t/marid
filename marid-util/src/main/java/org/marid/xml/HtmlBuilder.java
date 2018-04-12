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

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

public class HtmlBuilder extends HtmlAbstractBuilder<HtmlBuilder> {

  public HtmlBuilder() {
    this(documentBuilder().newDocument().createElement("html"));
    getDocument().appendChild(element);
  }

  private HtmlBuilder(Element element) {
    super(element);
  }

  @Override
  protected Transformer transformer(TransformerFactory factory) {
    final Transformer transformer = super.transformer(factory);
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.METHOD, "html");
    transformer.setOutputProperty(OutputKeys.VERSION, "5.0");
    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "about:legacy-compat");
    return transformer;
  }

  @Override
  public Node getNodeToTransform() {
    return getDocument();
  }

  @Override
  protected HtmlBuilder child(Element element) {
    return new HtmlBuilder(element);
  }

  static DocumentBuilder documentBuilder() {
    final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
    try {
      return documentBuilderFactory.newDocumentBuilder();
    } catch (Exception impossibleException) {
      throw new IllegalStateException(impossibleException);
    }
  }
}
