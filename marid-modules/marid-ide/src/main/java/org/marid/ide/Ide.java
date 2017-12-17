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

package org.marid.ide;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.marid.ide.panes.main.IdePane;
import org.marid.idelib.splash.MaridSplashCloseNotification;
import org.marid.idelib.spring.postprocessors.MaridCommonPostProcessor;
import org.marid.image.MaridIconFx;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.marid.ide.IdePrefs.PREFERENCES;

/**
 * @author Dmitry Ovchinnikov
 */
public class Ide extends Application {

  public static volatile Stage primaryStage;

  private final String style = PREFERENCES.get("style", STYLESHEET_MODENA);
  private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

  @Override
  public void init() {
    context.getBeanFactory().addBeanPostProcessor(new MaridCommonPostProcessor());
    context.register(IdeContext.class);
    context.setAllowBeanDefinitionOverriding(true);
    context.setAllowCircularReferences(false);

    new Thread(() -> {
      context.refresh();
      final IdePane idePane = context.getBean(IdePane.class);

      while (primaryStage == null) {
        Thread.onSpinWait();
      }

      Platform.runLater(() -> {
        context.start();
        primaryStage.setMinWidth(750.0);
        primaryStage.setMinHeight(550.0);
        primaryStage.setTitle("Marid IDE");
        primaryStage.setScene(new Scene(idePane,
            PREFERENCES.getDouble("width", 800),
            PREFERENCES.getDouble("height", 800)
        ));
        primaryStage.getIcons().addAll(MaridIconFx.getIcons(24, 32));
        primaryStage.setMaximized(PREFERENCES.getBoolean("maximized", false));
        primaryStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, event -> {
          PREFERENCES.putBoolean("maximized", primaryStage.isMaximized());
          if (!primaryStage.isMaximized()) {
            PREFERENCES.putDouble("width", primaryStage.getWidth());
            PREFERENCES.putDouble("height", primaryStage.getHeight());
          }
        });
        primaryStage.show();

        notifyPreloader(new MaridSplashCloseNotification());
      });
    }).start();
  }

  @Override
  public void start(Stage primaryStage) {
    Ide.primaryStage = primaryStage;
    setUserAgentStylesheet(style);
  }

  @Override
  public void stop() {
    context.close();
  }
}
