/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
