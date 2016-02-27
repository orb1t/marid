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

package org.marid.ide.icons.viewer;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.weathericons.WeatherIcon;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.marid.ide.icons.IdeIcons;
import org.marid.l10n.L10nSupport;

import javax.enterprise.context.Dependent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class IconViewerTable extends TableView<GlyphIcons> implements L10nSupport {

    public IconViewerTable() {
        super(icons());
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
        {
            final TableColumn<GlyphIcons, String> column = new TableColumn<>(s("Name"));
            column.setMinWidth(100);
            column.setPrefWidth(110);
            column.setMaxWidth(500);
            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().name()));
            getColumns().add(column);
        }
        {
            final TableColumn<GlyphIcons, String> column = new TableColumn<>(s("Font family"));
            column.setMinWidth(100);
            column.setPrefWidth(130);
            column.setMaxWidth(500);
            column.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getClass().getSimpleName()));
            getColumns().add(column);
        }
        {
            final TableColumn<GlyphIcons, GlyphIcon<?>> column = new TableColumn<>(s("Icon"));
            column.setMaxWidth(128);
            column.setPrefWidth(128);
            column.setMaxWidth(128);
            column.setCellValueFactory(param -> new SimpleObjectProperty<>(IdeIcons.glyphIcon(param.getValue(), 32)));
            column.setCellFactory(param -> new TableCell<GlyphIcons, GlyphIcon<?>>() {
                @Override
                protected void updateItem(GlyphIcon<?> item, boolean empty) {
                    super.updateItem(item, empty);
                    setGraphic(item);
                }
            });
            column.setSortable(false);
            column.setStyle("-fx-alignment: center");
            getColumns().add(column);
        }
    }

    private static ObservableList<GlyphIcons> icons() {
        final List<GlyphIcons> icons = Arrays.asList(
                OctIcon.values(),
                MaterialIcon.values(),
                MaterialDesignIcon.values(),
                FontAwesomeIcon.values(),
                WeatherIcon.values()
        ).stream()
                .flatMap(Stream::of)
                .sorted(Comparator.comparing(Enum::name))
                .collect(Collectors.toList());
        return FXCollections.unmodifiableObservableList(FXCollections.observableList(icons));
    }
}
