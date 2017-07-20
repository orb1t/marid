/*-
 * #%L
 * marid-runtime
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

package org.marid.runtime.context;

import org.marid.io.Xmls;
import org.marid.runtime.beans.Bean;
import org.w3c.dom.Element;

import javax.annotation.Nonnull;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
public final class MaridConfiguration {

    @Nonnull
    public final Bean[] beans;

    public MaridConfiguration(@Nonnull Bean... beans) {
        this.beans = beans;
    }

    public MaridConfiguration(@Nonnull Element element) {
        this.beans = Xmls.nodes(element, Element.class)
                .filter(e -> "beans".equals(e.getTagName()))
                .flatMap(e -> Xmls.nodes(e, Element.class))
                .filter(e -> "bean".equals(e.getTagName()))
                .map(Bean::new)
                .toArray(Bean[]::new);
    }

    public void writeTo(@Nonnull Element element) {
        for (final Bean bean : beans) {
            element.appendChild(build(element.getOwnerDocument().createElement("bean"), bean::writeTo));
        }
    }

    @Override
    public String toString() {
        return String.format("Context(%s)",
                Stream.of(beans).map(Bean::toString).collect(Collectors.joining(",\n\t", "\n\t", ""))
        );
    }
}
