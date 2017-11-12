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

package org.marid;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.ide.IdeContext;
import org.marid.ide.logging.IdeLogConsoleHandler;
import org.marid.ide.logging.IdeLogHandler;
import org.marid.ide.panes.main.IdePane;
import org.marid.image.MaridIconFx;
import org.marid.idelib.splash.MaridSplash;
import org.marid.idelib.spring.postprocessors.MaridCommonPostProcessor;
import org.marid.idelib.spring.ui.FxScope;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Locale;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.util.logging.Level.WARNING;
import static org.marid.ide.IdePrefs.PREFERENCES;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class Ide extends Application {

  public static final FxScope FX_SCOPE = new FxScope();

  public static volatile Stage primaryStage;

  private final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

  @Override
  public void init() throws Exception {
    context.getBeanFactory().addBeanPostProcessor(new MaridCommonPostProcessor());
    context.getBeanFactory().registerScope("fx", FX_SCOPE);
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
        primaryStage.setScene(new Scene(idePane, 1024, 768));
        primaryStage.getIcons().addAll(MaridIconFx.getIcons(24, 32));
        primaryStage.setMaximized(true);
        primaryStage.show();
      });
    }).start();
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    Ide.primaryStage = primaryStage;

    final Stage splash = new Stage(StageStyle.UNDECORATED);
    splash.setTitle("Marid");
    splash.getIcons().addAll(MaridIconFx.getIcons(24, 32));
    splash.setScene(new Scene(new MaridSplash(primaryStage, IdeLogHandler.LOG_RECORDS)));
    splash.show();

    setUserAgentStylesheet(PREFERENCES.get("style", STYLESHEET_MODENA));
  }

  @Override
  public void stop() throws Exception {
    context.close();
  }

  public static void main(String... args) throws Exception {
    // locale
    final String locale = PREFERENCES.get("locale", null);
    if (locale != null) {
      Locale.setDefault(Locale.forLanguageTag(locale));
    }

    // logging
    LogManager.getLogManager().reset();
    Logger.getLogger("").addHandler(new IdeLogHandler());
    Logger.getLogger("").addHandler(new IdeLogConsoleHandler());
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> log(WARNING, "Exception in {0}", e, t));

    // launch application
    Application.launch(args);
  }
}
