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

package org.marid.dependant.modbus;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.marid.dependant.modbus.annotation.DeviceIcon;
import org.marid.dependant.modbus.annotation.Modbus;
import org.marid.dependant.modbus.devices.AbstractDevice;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.MaridActions;
import org.marid.jfx.control.MaridControls;
import org.marid.jfx.icons.FontIcons;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;

import java.io.File;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Stream;

import static org.marid.jfx.LocalizedStrings.ls;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
@ComponentScan(basePackageClasses = {ModbusSourceConfiguration.class})
public class ModbusSourceConfiguration {

  @Bean(initMethod = "mkdirs")
  @Modbus
  public File baseDir() {
    return Paths.get(System.getProperty("user.home"), "marid", "modbus").toFile();
  }

  @Bean
  @Modbus
  public ToolBar topToolbar(@Modbus Collection<FxAction> actionMap) {
    return MaridActions.toolbar(actionMap);
  }

  @Bean
  @Modbus
  public ToolBar bottomToolbar(GenericApplicationContext context, ModbusPane pane) {
    final String[] beanNames = context.getBeanNamesForType(AbstractDevice.class, true, false);
    return new ToolBar(Stream.of(beanNames)
        .map(name -> {
          final Button button = new Button();
          final DeviceIcon icon = context.findAnnotationOnBean(name, DeviceIcon.class);
          if (icon != null) {
            button.setGraphic(FontIcons.glyphIcon(icon.value(), 20));
          }
          final Tooltip tooltip = new Tooltip();
          tooltip.textProperty().bind(ls(name));
          button.setTooltip(tooltip);
          button.setOnAction(event -> {
            final AbstractDevice<?> device = context.getBean(name, AbstractDevice.class);
            device.getProperties().put("name", name);
            pane.add(device);
          });
          return button;
        })
        .toArray(Node[]::new));
  }

  @Bean
  @Modbus
  public MenuBar menuBar(@Modbus Collection<FxAction> actionMap) {
    return new MenuBar(MaridActions.menus(actionMap));
  }

  @Bean
  @Modbus
  public BorderPane modbusRoot(@Modbus ToolBar topToolbar,
                               @Modbus MenuBar menuBar,
                               ModbusPane modbusPane,
                               @Modbus ToolBar bottomToolbar) {
    return new BorderPane(
        MaridControls.createMaridScrollPane(modbusPane),
        new VBox(menuBar, topToolbar),
        null,
        bottomToolbar,
        null
    );
  }

  @Bean
  @Modbus
  public Image image32() {
    return new Image("http://icons.iconarchive.com/icons/icons8/windows-8/32/Industry-Robot-icon.png");
  }

  @Bean
  @Modbus
  public Image image24() {
    return new Image("http://icons.iconarchive.com/icons/icons8/windows-8/24/Industry-Robot-icon.png");
  }

  @Bean(initMethod = "show")
  @Modbus
  public Stage modbusStage(@Modbus BorderPane modbusRoot, @Modbus Image[] images) {
    final Stage stage = new Stage(StageStyle.DECORATED);
    stage.getIcons().addAll(images);
    stage.setScene(new Scene(modbusRoot, 1024, 768));
    stage.titleProperty().bind(ls("MODBUS devices"));
    return stage;
  }
}
