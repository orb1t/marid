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

package org.marid.ide.tools.iconviewer;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.StageStyle;
import org.marid.ide.panes.main.IdePane;
import org.marid.l10n.L10n;
import org.marid.idelib.spring.annotation.PrototypeComponent;
import org.marid.idelib.spring.ui.FxStage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class IconViewer extends FxStage {

	public IconViewer() {
		super(StageStyle.DECORATED);
	}

	@Autowired
	private void configure(IdePane idePane, IconViewerTable table) {
		initOwner(idePane.getScene().getWindow());
		setTitle(L10n.s("Icon viewer"));
		final BorderPane pane = new BorderPane(table);
		pane.setPrefSize(800, 600);
		final Label countLabel = new Label(L10n.s("Icon count: %d", table.getItems().size()));
		pane.setBottom(countLabel);
		BorderPane.setMargin(table, new Insets(10, 10, 5, 10));
		BorderPane.setMargin(countLabel, new Insets(5, 10, 10, 10));
		setScene(new Scene(pane));
	}
}
