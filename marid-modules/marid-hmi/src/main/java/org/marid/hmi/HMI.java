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
import javafx.stage.Stage;
import org.jboss.logmanager.LogManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Dmitry Ovchinnikov
 */
public class HMI extends Application {

    private static final ClassPathXmlApplicationContext CONTEXT = new ClassPathXmlApplicationContext();

    @Override
    public void init() throws Exception {
        CONTEXT.setConfigLocations("classpath*:/META-INF/marid/beans.xml");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

    }

    public static void main(String... args) throws Exception {
        System.setProperty("java.util.logging.manager", LogManager.class.getName());
        launch(args);
    }
}
