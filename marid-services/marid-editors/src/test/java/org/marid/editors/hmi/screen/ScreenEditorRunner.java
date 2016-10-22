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

package org.marid.editors.hmi.screen;

import javafx.application.Application;
import javafx.stage.Stage;
import org.marid.spring.xml.BeanData;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class ScreenEditorRunner extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.getBeanFactory().registerSingleton("beanData", new BeanData());
        context.register(ScreenEditorConfiguration.class);
        context.refresh();
        context.start();
    }
}
