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

package org.marid.ide.common;

import org.marid.idelib.spring.annotation.IdeAction;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.dnd.DndManager;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ActionConfiguration {

  @IdeAction
  public Supplier<List<FxAction>> ideActions(@IdeAction ObjectFactory<List<FxAction>> actions,
                                             @IdeAction ObjectFactory<List<Spliterator<FxAction>>> actionQueues) {
    return () -> Stream.concat(
        actions.getObject().stream(),
        actionQueues.getObject().stream().flatMap(s -> StreamSupport.stream(s, false))
    ).collect(Collectors.toList());
  }

  @Bean
  public DndManager dndManager() {
    return new DndManager();
  }
}
