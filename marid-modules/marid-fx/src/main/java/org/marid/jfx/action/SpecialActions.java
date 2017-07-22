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
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

/**
 * @author Dmitry Ovchinnikov
 */
public class SpecialActions {

    private final List<SpecialAction> actionList;
    private final SpecialAction miscAction;

    public SpecialActions(List<SpecialAction> actionList, SpecialAction miscAction) {
        this.actionList = actionList;
        this.miscAction = miscAction;
    }

    public void reset() {
        actionList.forEach(a -> {
            a.reset();
            a.update();
        });
    }

    public void assign(Collection<FxAction> actions) {
        reset();
        final Function<FxAction, SpecialAction> gf = a -> a.specialAction != null ? a.specialAction : miscAction;
        final Map<SpecialAction, List<FxAction>> map = actions.stream().collect(groupingBy(gf, toList()));
        map.forEach((k, v) -> {
            if (v.size() == 1) {
                k.copy(v.get(0));
            } else {
                k.setChildren(v);
            }
            k.update();
        });
    }
}
