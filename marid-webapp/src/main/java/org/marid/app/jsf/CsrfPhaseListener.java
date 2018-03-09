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

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import javax.servlet.http.HttpServletResponse;

public class CsrfPhaseListener implements PhaseListener {
  @Override
  public void afterPhase(PhaseEvent event) {
  }

  @Override
  public void beforePhase(PhaseEvent event) {
    final FacesContext context = event.getFacesContext();

    final ELContext ec = context.getELContext();
    final ExpressionFactory ef = context.getApplication().getExpressionFactory();
    final String param = ef.createValueExpression(ec, "${_csrf.headerName}", String.class).getValue(ec).toString();
    final String value = ef.createValueExpression(ec, "${_csrf.token}", String.class).getValue(ec).toString();

    final ExternalContext externalContext = context.getExternalContext();
    final HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
    response.addHeader(param, value);
  }

  @Override
  public PhaseId getPhaseId() {
    return PhaseId.RENDER_RESPONSE;
  }
}
