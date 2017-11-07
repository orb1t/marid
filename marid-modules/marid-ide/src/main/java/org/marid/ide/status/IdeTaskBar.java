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

package org.marid.ide.status;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import org.marid.jfx.icons.FontIcons;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeTaskBar extends BorderPane {

	public IdeTaskBar(HBox taskBox) {
		final ScrollPane scrollPane = new ScrollPane(taskBox);
		setCenter(scrollPane);

		scrollPane.setBackground(new Background(new BackgroundFill(Color.TRANSPARENT, null, null)));
		scrollPane.setFitToWidth(false);
		scrollPane.setFitToHeight(true);
		scrollPane.setPannable(true);
		scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
		scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);

		taskBox.setFillHeight(true);
		taskBox.setAlignment(Pos.CENTER_LEFT);

		final Button left = new Button(null, FontIcons.glyphIcon("D_ARROW_LEFT_BOLD", 20));
		left.disableProperty().bind(scrollPane.hvalueProperty().lessThanOrEqualTo(0.0001));
		final InvalidationListener lChange = o -> {
			final boolean update = taskBox.getWidth() > scrollPane.getWidth();
			Platform.runLater(() -> setLeft(update ? left : null));
		};
		scrollPane.widthProperty().addListener(lChange);
		taskBox.widthProperty().addListener(lChange);
		setAlignment(left, Pos.CENTER_LEFT);
		setMargin(left, new Insets(0, 5, 0, 5));
		left.setOnAction(event -> {
			final double step = scrollPane.getWidth() / (taskBox.getWidth() * 2);
			scrollPane.setHvalue(max(0.0, scrollPane.getHvalue() - step));
		});

		final Button right = new Button(null, FontIcons.glyphIcon("D_ARROW_RIGHT_BOLD", 20));
		right.disableProperty().bind(scrollPane.hvalueProperty().greaterThan(0.9999));
		final InvalidationListener rChange = o -> {
			final boolean update = taskBox.getWidth() > scrollPane.getWidth();
			Platform.runLater(() -> setRight(update ? right : null));
		};
		scrollPane.widthProperty().addListener(rChange);
		taskBox.widthProperty().addListener(rChange);
		setAlignment(right, Pos.CENTER_LEFT);
		setMargin(right, new Insets(0, 5, 0, 5));
		right.setOnAction(event -> {
			final double step = scrollPane.getWidth() / (taskBox.getWidth() * 2);
			scrollPane.setHvalue(min(1.0, scrollPane.getHvalue() + step));
		});
	}
}
