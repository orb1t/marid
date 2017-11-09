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

package org.marid.ide.configurations;

import javafx.scene.input.KeyCodeCombination;
import org.marid.Ide;
import org.marid.jfx.action.FxAction;
import org.marid.idelib.spring.annotation.IdeAction;
import org.springframework.stereotype.Component;

import static javafx.scene.input.KeyCode.F12;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class WindowConfiguration {

	@IdeAction
	public FxAction alwaysOnTopAction() {
		return new FxAction("ops", "Window")
				.bindText("Always on top")
				.setIcon("D_BORDER_TOP")
				.setAccelerator(new KeyCodeCombination(F12, CONTROL_DOWN))
				.bindSelected(Ide.primaryStage.alwaysOnTopProperty())
				.setEventHandler(event -> Ide.primaryStage.setAlwaysOnTop(!Ide.primaryStage.isAlwaysOnTop()));
	}

	@IdeAction
	public FxAction fullScreenAction() {
		return new FxAction("ops", "Window")
				.bindText("Fullscreen")
				.setIcon("D_FULLSCREEN")
				.setAccelerator(new KeyCodeCombination(F12))
				.bindSelected(Ide.primaryStage.fullScreenProperty())
				.setEventHandler(event -> Ide.primaryStage.setFullScreen(!Ide.primaryStage.isFullScreen()));
	}
}
