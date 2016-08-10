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

package org.marid.spring.xml.data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.*;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "beans")
@XmlSeeAlso({BeanData.class})
@XmlAccessorType(XmlAccessType.NONE)
public class BeanFile implements AbstractData<BeanFile> {

    public final ObservableList<BeanData> beans = FXCollections.observableArrayList();

    public Stream<BeanData> allBeans() {
        final Stream.Builder<BeanData> builder = Stream.builder();
        beans.forEach(builder::add);
        return builder.build();
    }

    @XmlElement(name = "bean")
    public BeanData[] getBeans() {
        return beans.toArray(new BeanData[beans.size()]);
    }

    public void setBeans(BeanData[] beans) {
        this.beans.addAll(beans);
    }
}
