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

package org.marid.ide.beaned.data;

import de.jensd.fx.glyphs.GlyphIcon;
import de.jensd.fx.glyphs.GlyphIcons;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialicons.MaterialIcon;
import de.jensd.fx.glyphs.octicons.OctIcon;
import de.jensd.fx.glyphs.weathericons.WeatherIcon;
import javafx.scene.control.TreeItem;
import org.marid.beans.MaridBeanXml;
import org.marid.beans.MaridBeansXml;
import org.marid.ide.beaned.BeanEditorManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.icons.FontIcons;
import org.marid.logging.LogSupport;
import org.marid.misc.Builder;

import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.Closeable;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Function;

import static java.util.Comparator.comparing;
import static org.marid.xml.XmlBind.load;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanContext implements LogSupport, Closeable {

    private static final Map<String, Function<String, GlyphIcons>> ICONS = Builder.build(new HashMap<>(), map -> {
        map.put("FA", FontAwesomeIcon::valueOf);
        map.put("O", OctIcon::valueOf);
        map.put("MD", MaterialDesignIcon::valueOf);
        map.put("M", MaterialIcon::valueOf);
        map.put("W", WeatherIcon::valueOf);
    });

    public final ProjectProfile profile;
    public final URLClassLoader classLoader;
    public final TreeItem<Data> root = new TreeItem<>(RootData.ROOT_DATA);
    public final Set<MaridBeanXml> beansXmls = new TreeSet<>(comparing(b -> b.text != null ? b.text : b.type));
    public final BeanEditorManager beanEditorManager;

    private final Map<String, BeanInfo> classBeanInfo = new HashMap<>();
    private final Map<String, String> classIconMap = new HashMap<>();

    public BeanContext(ProjectProfile profile, BeanEditorManager beanEditorManager) {
        this.profile = profile;
        this.classLoader = profile.classLoader();
        this.beanEditorManager = beanEditorManager;
        try {
            for (final Enumeration<URL> e = classLoader.findResources("maridBeans.xml"); e.hasMoreElements(); ) {
                final URL url = e.nextElement();
                try (final InputStream inputStream = url.openStream()) {
                    final Source inputSource = new StreamSource(inputStream);
                    final MaridBeansXml beans = load(MaridBeansXml.class, inputSource, Unmarshaller::unmarshal);
                    beansXmls.addAll(beans.beans);
                } catch (Exception x) {
                    log(WARNING, "Unable to process {0}", x, url);
                }
            }
        } catch (Exception x) {
            log(WARNING, "Unable to enumerate marid beans", x);
        }
        beansXmls.forEach(b -> classIconMap.put(b.type, b.icon));
    }

    @Override
    public void close() {
        try {
            classLoader.close();
        } catch (Exception x) {
            log(WARNING, "Unable to close class loader", x);
        }
    }

    public String icon(String type) {
        return classIconMap.get(type);
    }

    public BeanInfo beanInfo(String type) {
        return classBeanInfo.computeIfAbsent(type, t -> {
            try {
                final Class<?> c = Class.forName(t, false, classLoader);
                return new BeanInfo(c);
            } catch (Exception x) {
                log(WARNING, "BeanInfo obtaining error for {0}", x, t);
                return new BeanInfo();
            }
        });
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
