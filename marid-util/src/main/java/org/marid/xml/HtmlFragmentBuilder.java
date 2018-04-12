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

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import java.util.Map;

public class HtmlFragmentBuilder extends HtmlAbstractBuilder<HtmlFragmentBuilder> {

  public HtmlFragmentBuilder(String tag, Map<String, ?> attrs) {
    this(HtmlBuilder.documentBuilder().newDocument().createElement(tag));
    getDocument().appendChild(element);
    $a(attrs);
  }

  private HtmlFragmentBuilder(Element element) {
    super(element);
  }

  @Override
  protected HtmlFragmentBuilder child(Element element) {
    return new HtmlFragmentBuilder(element);
  }

  @Override
  protected Transformer transformer(TransformerFactory factory) {
    final Transformer transformer = super.transformer(factory);
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.setOutputProperty(OutputKeys.METHOD, "html");
    transformer.setOutputProperty(OutputKeys.VERSION, "5.0");
    transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
    return transformer;
  }
}
