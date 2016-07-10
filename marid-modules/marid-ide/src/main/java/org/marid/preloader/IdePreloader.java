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

import com.sun.javafx.application.LauncherImpl;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.IdePrefs;
import org.marid.jfx.FxMaridIcon;
import org.marid.l10n.L10n;

import java.lang.reflect.Field;
import java.util.logging.Level;

import static org.marid.logging.LogSupport.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdePreloader extends Preloader {

    private final TextFlow log = new TextFlow();
    private final ScrollPane logScrollPane = new ScrollPane(log);
    private final ProgressBar progressBar = new ProgressBar();
    private Stage primaryStage;

    public IdePreloader() {
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
        final Label bottom = new Label(L10n.s("Data visualization and acquisition system"));
        bottom.setStyle("-fx-font-style: italic");
        box.getChildren().add(bottom);
        return box;
    }

    private GridPane preloaderPane() {
        final GridPane gridPane = new GridPane();
        gridPane.setEffect(new Lighting(new Light.Spot(10, 10, 1000, 0.5, new Color(1.0, 1.0, 1.0, 1.0))));
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        {
            final ColumnConstraints cc1 = new ColumnConstraints();
            cc1.setFillWidth(true);
            cc1.setHgrow(Priority.NEVER);

            final ColumnConstraints cc2 = new ColumnConstraints();
            cc2.setFillWidth(true);
            cc2.setHgrow(Priority.ALWAYS);

            gridPane.getColumnConstraints().addAll(cc1, cc2);
        }

        {
            final RowConstraints rc1 = new RowConstraints();
            rc1.setFillHeight(true);
            rc1.setVgrow(Priority.NEVER);

            final RowConstraints rc2 = new RowConstraints();
            rc2.setFillHeight(true);
            rc2.setVgrow(Priority.ALWAYS);

            final RowConstraints rc3 = new RowConstraints();
            rc3.setFillHeight(true);
            rc3.setVgrow(Priority.NEVER);

            gridPane.getRowConstraints().addAll(rc1, rc2, rc3);
        }
        gridPane.add(new ImageView(FxMaridIcon.maridIcon(64, Color.GREEN)), 0, 0);
        gridPane.add(titleBox(), 1, 0);
        gridPane.add(logScrollPane, 0, 1, 2, 1);
        gridPane.add(progressBar, 0, 2, 2, 1);
        return gridPane;
    }

    @Override
    public void handleApplicationNotification(PreloaderNotification info) {
        if (info instanceof LogNotification) {
            final LogNotification logNotification = (LogNotification) info;
            log.getChildren().addAll(logNotification.texts);
            logScrollPane.setVvalue(1.0);
        } else if (info instanceof CloseNotification) {
            primaryStage.close();
            try {
                final Field field = LauncherImpl.class.getDeclaredField("currentPreloader");
                field.setAccessible(true);
                field.set(null, null);
                log(Level.INFO, "Preloader closed and cleaned");
            } catch (Exception x) {
                log(Level.WARNING, "Unable to null the Preloader", x);
                log(Level.INFO, "Preloader closed");
            }
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        Application.setUserAgentStylesheet(IdePrefs.PREFERENCES.get("style", STYLESHEET_MODENA));
        primaryStage.initStyle(StageStyle.UNDECORATED);
        primaryStage.setScene(new Scene(preloaderPane(), 800, 800, true));
        primaryStage.show();
    }
}
