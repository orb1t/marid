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

package org.marid.ide.tools.iconviewer;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.marid.l10n.L10n;
import org.marid.idelib.spring.ui.FxComponent;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
@FxComponent
public class IconViewerTable extends TableView<String> {

  public IconViewerTable() {
    super(FXCollections.observableList(icons()));
    setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    {
      final TableColumn<String, String> column = new TableColumn<>(L10n.s("Name"));
      column.setMinWidth(100);
      column.setPrefWidth(110);
      column.setMaxWidth(500);
      column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue()));
      getColumns().add(column);
    }
    {
      final TableColumn<String, Node> column = new TableColumn<>(L10n.s("Icon"));
      column.setMaxWidth(128);
      column.setPrefWidth(128);
      column.setMaxWidth(128);
      column.setCellValueFactory(param -> new SimpleObjectProperty<>(glyphIcon(param.getValue(), 32)));
      column.setSortable(false);
      column.setStyle("-fx-alignment: center");
      getColumns().add(column);
    }
  }

  private static List<String> icons() {
    final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    final Properties properties = new Properties();
    try (final InputStream inputStream = classLoader.getResourceAsStream("fonts/meta.properties")) {
      properties.load(inputStream);
    } catch (IOException x) {
      throw new UncheckedIOException(x);
    }
    return properties.stringPropertyNames().stream().sorted().collect(Collectors.toList());
  }
}
