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

package org.marid.ide.common;

import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.beans.factory.ObjectFactory;
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
}
