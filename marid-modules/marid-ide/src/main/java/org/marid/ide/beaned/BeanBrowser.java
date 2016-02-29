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

package org.marid.ide.beaned;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.weathericons.WeatherIcon;
import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.marid.beans.MaridBeanXml;
import org.marid.beans.MaridBeansXml;
import org.marid.jfx.icons.FontIcons;
import org.marid.logging.LogSupport;
import org.marid.misc.Builder;
import org.marid.xml.XmlBind;

import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.comparing;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanBrowser extends ListView<MaridBeanXml> implements LogSupport {

    private static final Map<String, Function<String, GlyphIcons>> ICONS = Builder.build(new HashMap<>(), map -> {
        map.put("FA", FontAwesomeIcon::valueOf);
        map.put("O", OctIcon::valueOf);
        map.put("MD", MaterialDesignIcon::valueOf);
        map.put("M", MaterialIcon::valueOf);
        map.put("W", WeatherIcon::valueOf);
    });

    private final BeanEditorPane editorPane;
    private final Map<String, String> classIconMap = new HashMap<>();

    public BeanBrowser(BeanEditorPane editorPane) {
        this.editorPane = editorPane;
        final Set<MaridBeanXml> beansXmls = new TreeSet<>(comparing(b -> b.text != null ? b.text : b.type));
        try {
            for (final Enumeration<URL> e = editorPane.classLoader.findResources("maridBeans.xml"); e.hasMoreElements(); ) {
                final URL url = e.nextElement();
                try {
                    final Source inputSource = new StreamSource(url.toExternalForm());
                    final MaridBeansXml beans = XmlBind.load(MaridBeansXml.class, inputSource, Unmarshaller::unmarshal);
                    beansXmls.addAll(beans.beans);
                } catch (Exception x) {
                    log(WARNING, "Unable to process {0}", x, url);
                }
            }
        } catch (Exception x) {
            log(WARNING, "Unable to enumerate marid beans", x);
        }
        beansXmls.forEach(b -> classIconMap.put(b.type, b.icon));
        setItems(FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(beansXmls)));
        setCellFactory(param -> new ListCell<MaridBeanXml>() {
            @Override
            protected void updateItem(MaridBeanXml item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.text != null ? item.text : item.type);
                    setGraphic(icon(item.icon, 16, OctIcon.BOOK));
                }
            }
        });
    }

    public String icon(String type) {
        return classIconMap.get(type);
    }

    public static GlyphIcon<?> icon(String text, int size, GlyphIcons defaultIcon) {
        if (text != null) {
            final String[] parts = text.split("[.]");
            if (parts.length == 2) {
                final Function<String, GlyphIcons> func = ICONS.getOrDefault(parts[0], FontAwesomeIcon::valueOf);
                try {
                    return FontIcons.glyphIcon(func.apply(parts[1]), size);
                } catch (IllegalArgumentException x) {
                    // ignore
                }
            }
        }
        return FontIcons.glyphIcon(defaultIcon, size);
    }
}
