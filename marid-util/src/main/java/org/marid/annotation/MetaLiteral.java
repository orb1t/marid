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
