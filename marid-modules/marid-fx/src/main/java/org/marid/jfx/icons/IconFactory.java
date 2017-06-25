package org.marid.jfx.icons;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import org.jetbrains.annotations.PropertyKey;

import java.util.logging.Level;

/**
 * @author Dmitry Ovchinnikov
 */
public interface IconFactory {

    static Node icon(Level level) {
        switch (level.intValue()) {
            case Integer.MAX_VALUE: return icon("D_SELECT_OFF", Color.RED);
            case Integer.MIN_VALUE: return icon("D_ARROW_ALL", Color.GREEN);
            case 1000: return icon("M_ERROR", Color.RED);
            case 900: return icon("F_WARNING", Color.ORANGE);
            case 800: return icon("F_INFO_CIRCLE", Color.BLUE);
            case 700: return icon("M_CONTROL_POINT", Color.GREEN);
            case 500: return icon("D_BATTERY_60", Color.GREEN);
            case 400: return icon("D_BATTERY_80", Color.GREEN);
            case 300: return icon("D_BATTERY_CHARGING_100", Color.GREEN);
            default: return icon("D_BATTERY_UNKNOWN", Color.GRAY);
        }
    }

    static Node icon(@PropertyKey(resourceBundle = "fonts.meta") String icon, Paint paint, int size) {
        final Text glyphIcon = FontIcons.glyphIcon(icon, size);
        glyphIcon.setFill(paint);
        return glyphIcon;
    }

    static Node icon(@PropertyKey(resourceBundle = "fonts.meta") String icon, Paint paint) {
        return icon(icon, paint, 16);
    }
}
