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

import javafx.beans.Observable;
import org.marid.jfx.beans.FxList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Collectors;

import static org.marid.misc.Iterables.nodes;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "beans")
@XmlSeeAlso({BeanData.class})
@XmlAccessorType(XmlAccessType.NONE)
public final class BeanFile extends AbstractData<BeanFile> {

    public final FxList<String> path = new FxList<>();
    public final FxList<BeanData> beans = new FxList<>(d -> new Observable[] {d});

    public BeanFile() {
        path.addListener(this::fireInvalidate);
        beans.addListener(this::fireInvalidate);
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

    @Override
    public String toString() {
        return String.format("BeanFile(%s,%d)", getFilePath(), beans.size());
    }

    @Override
    public void loadFrom(Document document, Element element) {
        nodes(element, Element.class).filter(e -> "bean".equals(e.getTagName())).forEach(e -> {
            final BeanData beanData = new BeanData();
            beanData.loadFrom(document, e);
            beans.add(beanData);
        });
    }

    @Override
    public void writeTo(Document document, Element element) {
        beans.stream().filter(b -> !b.isEmpty()).forEach(b -> {
            final Element e = document.createElement("bean");
            b.writeTo(document, e);
            element.appendChild(e);
        });
    }
}
