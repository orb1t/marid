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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.*;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "beans")
@XmlSeeAlso({BeanData.class})
@XmlAccessorType(XmlAccessType.NONE)
public final class BeanFile extends AbstractData<BeanFile> {

    public final ObservableList<String> path = FXCollections.observableArrayList();
    public final ObservableList<BeanData> beans = FXCollections.observableArrayList(BeanData::observables);

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

    public Path path(Path base) {
        return path.stream().reduce(base, Path::resolve, (a1, a2) -> a2);
    }

    public String getFilePath() {
        return path.stream().collect(Collectors.joining("/"));
    }

    public static Comparator<BeanFile> asc() {
        return (f1, f2) -> {
            final int min = Math.min(f1.path.size(), f2.path.size());
            for (int i = 0; i < min; i++) {
                final String p1 = f1.path.get(i);
                final String p2 = f2.path.get(i);
                final int c = p1.compareTo(p2);
                if (c != 0) {
                    return c;
                }
            }
            return Integer.compare(f1.path.size(), f2.path.size());
        };
    }

    public static BeanFile beanFile(Path basePath, Path path) {
        final BeanFile file = new BeanFile();
        for (final Path p : basePath.relativize(path)) {
            file.path.add(p.toString());
        }
        return file;
    }
}
