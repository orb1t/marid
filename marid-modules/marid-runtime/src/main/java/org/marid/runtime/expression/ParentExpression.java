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

import org.marid.runtime.context2.BeanContext;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.NoSuchElementException;

import static java.lang.Integer.parseInt;
import static org.marid.io.Xmls.attribute;

public class ParentExpression extends Expression {

    private final int level;

    public ParentExpression(int level) {
        this.level = level;
    }

    public ParentExpression(@Nonnull Element element) {
        level = parseInt(attribute(element, "level").orElseThrow(() -> new NullPointerException("level")));
    }

    public int getLevel() {
        return level;
    }

    @Nonnull
    @Override
    public String getTag() {
        return "parent";
    }

    @Override
    public void saveTo(@Nonnull Element element) {
        element.setAttribute("level", Integer.toString(level));
    }

    @Override
    protected Object execute(@Nullable Object self, @Nonnull BeanContext context) {
        int l = 0;
        for (BeanContext c = context; c != null; c = c.getParent(), l++) {
            if (level == l) {
                return c.getInstance();
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public String toString() {
        return "$" + level;
    }
}
