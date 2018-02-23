/*-
 * #%L
 * marid-runtime
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

package org.marid.expression.xml;

import org.jetbrains.annotations.NotNull;
import org.marid.runtime.common.MaridPlaceholderResolver;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlPlaceholderProcessor {

  @NotNull
  private final MaridPlaceholderResolver resolver;

  public XmlPlaceholderProcessor(@NotNull MaridPlaceholderResolver resolver) {
    this.resolver = resolver;
  }

  public void process(@NotNull Node node) {
    switch (node.getNodeType()) {
      case Node.ELEMENT_NODE:
        switch (node.getNodeName()) {
          case "string":
            node.setTextContent(resolver.resolvePlaceholders(node.getTextContent()));
            break;
        }
        break;
    }

    final NodeList children = node.getChildNodes();

    for (int i = 0; i < children.getLength(); i++) {
      final Node child = children.item(i);
      process(child);
    }
  }
}
