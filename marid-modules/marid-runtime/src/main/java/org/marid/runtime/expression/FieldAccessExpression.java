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

package org.marid.runtime.expression;

import org.marid.io.Xmls;
import org.w3c.dom.Element;

public class FieldAccessExpression extends Expression {

    private final String target;
    private final String field;

    public FieldAccessExpression(String target, String field) {
        this.target = target;
        this.field = field;
    }

    public FieldAccessExpression(Element element) {
        target = Xmls.attribute(element, "target").orElseThrow(() -> new NullPointerException("target"));
        field = Xmls.attribute(element, "field").orElseThrow(() -> new NullPointerException("field"));
    }

    public String getTarget() {
        return target;
    }

    public String getField() {
        return field;
    }

    @Override
    public String getTag() {
        return "get";
    }

    @Override
    public void saveTo(Element element) {
        element.setAttribute("target", target);
        element.setAttribute("field", field);
    }

    @Override
    public String toString() {
        return target + "." + field;
    }
}
