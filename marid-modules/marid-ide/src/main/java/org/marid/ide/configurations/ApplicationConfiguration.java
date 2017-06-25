package org.marid.ide.configurations;

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

import javafx.application.Platform;
import org.marid.jfx.action.FxAction;
import org.marid.spring.annotation.IdeAction;
import org.springframework.stereotype.Component;


/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class ApplicationConfiguration {

    @IdeAction
    public FxAction exitAction() {
        return new FxAction("x", "Application")
                .bindText("Exit")
                .setIcon("D_EXIT_TO_APP")
                .setEventHandler(event -> Platform.exit());
    }
}
