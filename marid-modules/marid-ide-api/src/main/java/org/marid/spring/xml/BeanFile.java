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

package org.marid.spring.xml;

import javafx.beans.InvalidationListener;
import javafx.collections.ObservableList;
import org.marid.util.MaridCollections;

import javax.xml.bind.annotation.*;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "beans")
@XmlSeeAlso({BeanData.class})
@XmlAccessorType(XmlAccessType.NONE)
public final class BeanFile extends AbstractData<BeanFile> {

    public final ObservableList<BeanData> beans = MaridCollections.list();

    public BeanFile() {
        final InvalidationListener invalidationListener = o -> invalidate();
        beans.addListener(invalidationListener);
    }

    public Stream<BeanData> allBeans() {
        final Stream.Builder<BeanData> builder = Stream.builder();
        beans.forEach(builder::add);
        return builder.build();
    }

    @XmlElement(name = "bean")
    public BeanData[] getBeans() {
        return beans.stream().filter(b -> !b.isEmpty()).toArray(BeanData[]::new);
    }

    public void setBeans(BeanData[] beans) {
        this.beans.addAll(beans);
    }
}
