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
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;
import java.util.stream.IntStream;

import static java.lang.Thread.currentThread;
import static java.util.stream.Collectors.toList;
import static javafx.scene.paint.Color.GREEN;
import static org.marid.jfx.FxMaridIcon.maridIcon;
import static org.marid.misc.Casts.cast;
import static org.marid.runtime.MaridContextInitializer.applicationContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class HmiApplication extends Application {

    private final GenericXmlApplicationContext context = applicationContext(currentThread().getContextClassLoader());

    @Override
    public void init() throws Exception {
        final MapPropertySource cmdProps = new MapPropertySource("cmd", cast(getParameters().getNamed()));
        context.getBeanFactory().registerSingleton("application", this);
        context.getBeanFactory().addBeanPostProcessor(new HmiPostProcessor());
        context.getEnvironment().getPropertySources().addFirst(cmdProps);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        final HmiPane pane = new HmiPane();
        context.addApplicationListener(event -> {
            if (event instanceof ContextClosedEvent) {
                if (Platform.isFxApplicationThread()) {
                    primaryStage.close();
                } else {
                    Platform.runLater(primaryStage::close);
                }
            }
        });
        context.refresh();
        context.start();
        primaryStage.setScene(new Scene(pane, 800, 600));
        primaryStage.getIcons().addAll(IntStream.of(16, 24, 32).mapToObj(s -> maridIcon(s, GREEN)).collect(toList()));
        primaryStage.setOnCloseRequest(event -> context.close());
        primaryStage.show();
        final Map<String, Stage> stageMap = context.getBeansOfType(Stage.class, true, true);
        pane.addStages(stageMap);
        stageMap.forEach((name, stage) -> {
            if (stage.getIcons().isEmpty()) {
                stage.getIcons().addAll(primaryStage.getIcons());
            }
            stage.show();
        });
    }
}
