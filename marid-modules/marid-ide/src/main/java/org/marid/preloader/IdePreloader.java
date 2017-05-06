/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.preloader;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.Bloom;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.IdePrefs;
import org.marid.image.MaridIconFx;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static javafx.scene.paint.Color.GREEN;
import static org.marid.ide.logging.IdeLogConfig.ROOT_LOGGER;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdePreloader extends Preloader {

    private final IdePreloaderLogHandler logHandler;
    private final TextFlow log = new TextFlow();
    private final ScrollPane logScrollPane = new ScrollPane(log);
    private final ProgressBar progressBar = new ProgressBar();
    private final AtomicInteger counter = new AtomicInteger();
    private final int maxPreloaderMessages = IdePrefs.PREFERENCES.getInt("maxPreloaderMessages", 100);
    private Stage primaryStage;

    public IdePreloader() {
        logHandler = new IdePreloaderLogHandler(this);
        logScrollPane.setFitToHeight(true);
        logScrollPane.setFitToWidth(true);
        logScrollPane.setFocusTraversable(false);
        log.setStyle("-fx-font-family: monospace; -fx-font-size: 80%");
        log.setPadding(new Insets(5, 5, 5, 5));
        progressBar.setMaxWidth(1000);
    }

    private VBox titleBox() {
        final VBox box = new VBox();
        box.setSpacing(2);
        box.setFillWidth(true);
        box.setAlignment(Pos.CENTER_LEFT);
        final Label top = new Label("Marid IDE");
        top.setStyle("-fx-font-size: 200%; -fx-font-weight: bold");
        box.getChildren().add(top);
        box.getChildren().add(new Separator(Orientation.HORIZONTAL));
        final Label bottom = new Label(s("Data visualization and acquisition system"));
        bottom.setStyle("-fx-font-style: italic");
        box.getChildren().add(bottom);
        return box;
    }

    private GridPane preloaderPane() {
        final GridPane gridPane = new GridPane();
        gridPane.setEffect(new Blend(BlendMode.DARKEN, new Bloom(0.97), new Bloom(0.96)));
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.getColumnConstraints().add(build(new ColumnConstraints(), cc -> {
            cc.setFillWidth(true);
            cc.setHgrow(Priority.NEVER);
        }));
        gridPane.getColumnConstraints().add(build(new ColumnConstraints(), cc -> {
            cc.setFillWidth(true);
            cc.setHgrow(Priority.ALWAYS);
        }));
        gridPane.getRowConstraints().add(build(new RowConstraints(), rc -> {
            rc.setFillHeight(true);
            rc.setVgrow(Priority.NEVER);
        }));
        gridPane.getRowConstraints().add(build(new RowConstraints(), rc -> {
            rc.setFillHeight(true);
            rc.setVgrow(Priority.ALWAYS);
        }));
        gridPane.getRowConstraints().add(build(new RowConstraints(), rc -> {
            rc.setFillHeight(true);
            rc.setVgrow(Priority.NEVER);
        }));
        gridPane.addRow(0, new ImageView(MaridIconFx.getImage(64, GREEN)), titleBox());
        gridPane.add(logScrollPane, 0, 1, 2, 1);
        gridPane.add(progressBar, 0, 2, 2, 1);
        return gridPane;
    }

    private void publishTextsSync(List<Text> texts) {
        log.getChildren().addAll(texts);
        final int count = counter.addAndGet(texts.size());
        final double progress = count / (double) maxPreloaderMessages;
        progressBar.setProgress(progress);
        logScrollPane.setVvalue(1.0);
    }

    private void publishText(Text text) {
        publishTextsSync(Collections.singletonList(text));
    }

    public void publishTexts(List<Text> texts) {
        Platform.runLater(() -> publishTextsSync(texts));
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification info) {
        switch (info.getType()) {
            case BEFORE_INIT:
                ROOT_LOGGER.addHandler(logHandler);
                publishText(new Text(m("Loader initialized") + System.lineSeparator()));
                break;
            case BEFORE_LOAD:
                publishText(new Text(m("Loading application...") + System.lineSeparator()));
                break;
            case BEFORE_START:
                publishText(new Text(m("Starting application...") + System.lineSeparator()));
                logHandler.close();
                primaryStage.close();
                break;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        Application.setUserAgentStylesheet(IdePrefs.PREFERENCES.get("style", STYLESHEET_MODENA));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(preloaderPane(), 600, 600, true));
        primaryStage.show();
    }
}
