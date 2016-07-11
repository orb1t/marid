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

package org.marid.hmi;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.marid.runtime.MaridConsoleExitHandler;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static java.lang.Thread.currentThread;
import static org.marid.misc.Casts.cast;
import static org.marid.runtime.MaridContextInitializer.applicationContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class HmiApplication extends Application {

    private final GenericXmlApplicationContext context = applicationContext(currentThread().getContextClassLoader());

    public static HmiApplication application;

    @Override
    public void init() throws Exception {
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource("cmd", cast(getParameters().getNamed())));
        final Thread thread = new Thread(() -> MaridConsoleExitHandler.handle(() -> Platform.runLater(context::close)));
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        HmiApplication.application = this;
        context.load("classpath*:/META-INF/hmi/**/*.xml");
        context.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                primaryStage.close();
            }
        });
        context.refresh();
        context.start();
        primaryStage.setOnCloseRequest(event -> context.close());
        primaryStage.setScene(new Scene(context.getBean(HmiPane.class), 800, -1));
        primaryStage.show();
        final Map<String, Stage> stageMap = context.getBeansOfType(Stage.class, true, true);
        stageMap.forEach((name, stage) -> {
            try {
                stage.show();
            } catch (Exception x) {
                throw new IllegalStateException("Unable to show " + name, x);
            }
        });
    }
}
