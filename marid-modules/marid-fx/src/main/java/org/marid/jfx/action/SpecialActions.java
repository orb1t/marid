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

import javafx.scene.control.SelectionModel;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public class SpecialActions {

    private final List<SpecialAction> actionList;

    public SpecialActions(List<SpecialAction> actionList) {
        this.actionList = actionList;
    }

    public <T> void setup(@Nonnull SelectionModel<T> model, @Nonnull Function<T, Collection<FxAction>> actions) {
        model.selectedItemProperty().addListener((o, oV, nV) -> {
            final Collection<FxAction> map = actions.apply(nV);
            final Map<SpecialAction, Collection<FxAction>> specialActions = new IdentityHashMap<>();
            map.forEach(v -> {
                if (v.specialAction != null) {
                    specialActions.computeIfAbsent(v.specialAction, key -> new ArrayList<>()).add(v);
                }
            });
            specialActions.forEach((k, v) -> {
                if (v.size() == 1) {
                    final FxAction action = v.iterator().next();
                    k.reset();
                    k.copy(action);
                    k.update();
                } else {
                    k.reset();
                    k.children.addAll(v);
                    k.update();
                }
            });
            actionList.stream().filter(v -> !specialActions.containsKey(v)).forEach(a -> {
                a.reset();
                a.update();
            });
        });
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
}
