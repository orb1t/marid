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

import javafx.collections.FXCollections;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import org.marid.beans.MaridBeanXml;
import org.marid.beans.MaridBeansXml;
import org.marid.logging.LogSupport;
import org.marid.xml.XmlBind;

import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Comparator.comparing;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanBrowser extends ListView<MaridBeanXml> implements LogSupport {

    public BeanBrowser(URLClassLoader classLoader) {
        final Set<MaridBeanXml> beansXmls = new TreeSet<>(comparing(b -> b.text != null ? b.text : b.type));
        try {
            for (final Enumeration<URL> e = classLoader.findResources("maridBeans.xml"); e.hasMoreElements(); ) {
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
        setItems(FXCollections.unmodifiableObservableList(FXCollections.observableArrayList(beansXmls)));
        setCellFactory(param -> new ListCell<MaridBeanXml>() {
            @Override
            protected void updateItem(MaridBeanXml item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && !empty) {
                    setText(item.text != null ? item.text : item.type);
                }
            }
        });
    }
}
