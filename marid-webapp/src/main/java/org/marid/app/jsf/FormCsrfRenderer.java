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

package org.marid.app.jsf;

import com.sun.faces.renderkit.html_basic.FormRenderer;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import java.io.IOException;

public class FormCsrfRenderer extends FormRenderer {

  @Override
  public void encodeEnd(FacesContext context, UIComponent component) throws IOException {
    final ELContext ec = context.getELContext();
    final ExpressionFactory ef = context.getApplication().getExpressionFactory();
    final ResponseWriter writer = context.getResponseWriter();

    final Object param = ef.createValueExpression(ec, "${_csrf.parameterName}", String.class).getValue(ec);
    final Object value = ef.createValueExpression(ec, "${_csrf.token}", String.class).getValue(ec);

    writer.startElement("input", component);
    writer.writeAttribute("type", "hidden", null);
    writer.writeAttribute("name", param, null);
    writer.writeAttribute("value", value, null);
    writer.endElement("input");
    writer.write("\n");

    super.encodeEnd(context, component);
  }
}
