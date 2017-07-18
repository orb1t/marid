/*-
 * #%L
 * marid-util
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

package org.marid.annotation;

import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov
 */
public class MetaLiteral {

    public final String name;
    public final String icon;
    public final String description;

    public MetaLiteral(String name, String icon, String description) {
        this.name = name;
        this.icon = icon;
        this.description = description;
    }

    public MetaLiteral(MetaInfo metaInfo) {
        this(metaInfo.name(), metaInfo.icon(), metaInfo.description());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else  if (o == null || getClass() != o.getClass()) {
            return false;
        } else {
            final MetaLiteral that = (MetaLiteral) o;
            return Objects.equals(name, that.name) &&
                    Objects.equals(icon, that.icon) &&
                    Objects.equals(description, that.description);
        }
    }

    public static MetaLiteral l(String name, String icon, String description) {
        return new MetaLiteral(name, icon, description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, icon, description);
    }

    @Override
    public String toString() {
        return String.format("Meta(%s,%s,%s)", name, icon, description);
    }
}
