package org.marid.jfx.props;

import javafx.beans.property.ObjectProperty;
import javafx.scene.Node;
import org.marid.jfx.icons.FontIcons;

/**
 * @author Dmitry Ovchinnikov
 */
@SuppressWarnings("unchecked")
public interface GraphicIcons<T extends GraphicIcons<T>> {

    ObjectProperty<Node> graphicProperty();

    default T icon(String icon, int size) {
        graphicProperty().set(FontIcons.glyphIcon(icon, size));
        return (T) this;
    }

    default T icon(String icon) {
        return icon(icon, 16);
    }
}
