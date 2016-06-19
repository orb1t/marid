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
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jboss.logmanager.LogManager;
import org.marid.runtime.MaridConsoleExitHandler;
import org.marid.runtime.MaridStarter;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import java.util.Map;

import static java.lang.Thread.currentThread;
import static org.marid.runtime.MaridContextInitializer.applicationContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class HmiLauncher extends Application implements MaridStarter {

    private final GenericApplicationContext context = applicationContext(currentThread().getContextClassLoader());

    @Override
    public void init() throws Exception {
        final Thread thread = new Thread(() -> MaridConsoleExitHandler.handle(context));
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void start(String... args) throws Exception {
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        context.getEnvironment().getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
        Application.launch(HmiLauncher.class, args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final HmiPane hmiPane = new HmiPane();
        context.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.registerSingleton("primaryStage", primaryStage);
            beanFactory.registerSingleton("application", this);
            beanFactory.registerSingleton("hmiPane", hmiPane);
        });
        context.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                primaryStage.close();
            }
        });
        context.refresh();
        context.start();
        primaryStage.setScene(new Scene(hmiPane, 800, -1));
        primaryStage.show();
        final Map<String, Stage> stageMap = context.getBeansOfType(Stage.class, true, true);
        stageMap.forEach((name, stage) -> {
            if (stage != primaryStage) {
                try {
                    stage.show();
                } catch (Exception x) {
                    throw new IllegalStateException("Unable to show " + name, x);
                }
            }
        });
    }

    @Override
    public void stop() throws Exception {
        context.close();
    }
}
