/*-
 * #%L
 * marid-ide
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

package org.marid.jfx.action;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class SpecialActions {

    private final List<SpecialAction> actionList;

    public SpecialActions(List<SpecialAction> actionList) {
        this.actionList = actionList;
    }

    public List<SpecialAction> getActionList() {
        return actionList;
    }

    public void reset() {
        actionList.forEach(a -> {
            a.reset();
            a.update();
        });
    }

    public void assign(Map<SpecialAction, Collection<FxAction>> map) {
        reset();
        map.forEach((k, v) -> {
            k.reset();
            if (v.size() == 1) {
                k.copy(v.iterator().next());
            } else {
                k.children.addAll(v);
            }
            k.update();
        });
    }
}
