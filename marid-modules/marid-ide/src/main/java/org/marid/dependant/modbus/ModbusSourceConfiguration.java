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

package org.marid.dependant.modbus;

import javafx.scene.Scene;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.dependant.modbus.annotation.Modbus;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.jfx.panes.MaridScrollPane;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
@ComponentScan(basePackageClasses = {ModbusSourceConfiguration.class})
public class ModbusSourceConfiguration {

    @Bean
    @Modbus
    public ToolBar modbusToolbar(@Modbus Map<String, FxAction> actionMap) {
        return new ToolBar(MaridActions.toolbar(actionMap));
    }

    @Bean
    @Modbus
    public MenuBar menuBar(@Modbus Map<String, FxAction> actionMap) {
        return new MenuBar(MaridActions.menus(actionMap));
    }

    @Bean
    @Modbus
    public BorderPane modbusRoot(@Modbus ToolBar toolbar,
                                 @Modbus MenuBar menuBar,
                                 ModbusPane modbusPane) {
        return new BorderPane(new MaridScrollPane(modbusPane), new VBox(menuBar, toolbar), null, null, null);
    }

    @Bean
    @Modbus
    public Image image32() {
        return new Image("http://icons.iconarchive.com/icons/icons8/windows-8/32/Industry-Robot-icon.png");
    }

    @Bean(initMethod = "show")
    @Modbus
    public Stage modbusStage(@Modbus BorderPane modbusRoot, @Modbus Image[] images) {
        final Stage stage = new Stage(StageStyle.DECORATED);
        stage.getIcons().addAll(images);
        stage.setScene(new Scene(modbusRoot, 1024, 768));
        return stage;
    }
}
