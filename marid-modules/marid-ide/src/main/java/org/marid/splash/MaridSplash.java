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

package org.marid.splash;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.CacheHint;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.effect.MotionBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.marid.IdePrefs;
import org.marid.image.MaridIconFx;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.LogRecord;

import static javafx.scene.layout.BorderStrokeStyle.SOLID;
import static javafx.scene.paint.Color.GRAY;
import static org.marid.l10n.L10n.m;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridSplash extends BorderPane implements AutoCloseable {

    private static final Color BACKGROUND = Color.DIMGRAY.darker().darker();

    private final ObservableList<LogRecord> records;
    private final ListChangeListener<LogRecord> listener;
    private final RotateTransition transition;
    private final AtomicInteger counter = new AtomicInteger(1);
    private final int maxCount;
    private final Font monospaced;
    private final TextFlow flow;

    public MaridSplash(Stage stage, ObservableList<LogRecord> logRecords) {
        stage.setOnShown(event -> {
            close();
            stage.setOnShown(null);
        });
        records = logRecords;
        maxCount = IdePrefs.PREFERENCES.getInt("splashMaxLogRecords", 100);
        setBackground(new Background(new BackgroundFill(BACKGROUND, null, null)));
        setBorder(new Border(new BorderStroke(GRAY, SOLID, null, new BorderWidths(3))));
        setCacheHint(CacheHint.SPEED);

        final ImageView image = new ImageView(MaridIconFx.getImage(32, Color.GREEN));
        image.setCacheHint(CacheHint.SPEED);
        image.setClip(new Circle(16, 16, 16));

        final Label title = new Label("Marid IDE", image);
        title.setPadding(new Insets(16));
        title.setGraphicTextGap(10);
        title.setPrefWidth(800);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("System", FontWeight.BOLD, 24));
        title.setEffect(new MotionBlur(30, 2));

        setTop(title);

        transition = new RotateTransition(Duration.millis(1000), image);
        transition.setFromAngle(0);
        transition.setToAngle(360);
        transition.setCycleCount(Animation.INDEFINITE);

        final Color background = Color.DARKGRAY.deriveColor(1, 1, 0.2, 1);
        flow = new TextFlow();
        flow.setBackground(new Background(new BackgroundFill(background, null, null)));
        flow.setPrefHeight(400);
        flow.setLineSpacing(4);

        final ScrollPane scrollPane = new ScrollPane(flow);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setBackground(new Background(new BackgroundFill(background, null, null)));
        scrollPane.setHbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollBarPolicy.NEVER);
        scrollPane.setEffect(new MotionBlur(30, 2));

        final StackPane group = new StackPane(scrollPane);
        group.setPadding(new Insets(0, 16, 0, 16));

        setCenter(group);

        final ProgressBar progressBar = new ProgressBar();
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPadding(new Insets(16));
        progressBar.setBackground(new Background(new BackgroundFill(BACKGROUND, null, null)));
        setBottom(progressBar);

        monospaced = Font.font("Monospaced", FontWeight.NORMAL, 10);
        listener = c -> {
            progressBar.setProgress(Math.min(counter.getAndIncrement() / (double) maxCount, 1.0));
            while (c.next()) {
                if (c.wasAdded()) {
                    if (!c.getAddedSubList().isEmpty()) {
                        flow.getChildren().addAll(texts(c.getAddedSubList()));
                        flow.layout();
                        scrollPane.setVvalue(1.0);
                    }
                }
            }
        };
    }

    public void init() {
        flow.getChildren().addAll(texts(records));
        records.addListener(listener);
        transition.play();
    }

    private Text[] texts(List<? extends LogRecord> logRecords) {
        return logRecords.parallelStream()
                .map(r -> new Text(m(r.getMessage(), r.getParameters()) + System.lineSeparator()))
                .peek(t -> t.setFont(monospaced))
                .peek(t -> t.setFill(Color.LIGHTGREEN))
                .toArray(Text[]::new);
    }

    @Override
    public void close() {
        IdePrefs.PREFERENCES.putInt("splashMaxLogRecords", Math.max(counter.get(), 100));
        transition.stop();
        records.removeListener(listener);
        getScene().getWindow().hide();
    }
}
