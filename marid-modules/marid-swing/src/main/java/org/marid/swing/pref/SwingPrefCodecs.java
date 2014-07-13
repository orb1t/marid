/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.swing.pref;

import org.marid.pref.PrefCodecs;
import org.marid.pref.PrefReader;
import org.marid.pref.PrefWriter;

import javax.swing.*;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.TextAttribute;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

import static java.lang.Integer.parseInt;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingPrefCodecs extends PrefCodecs {
    @Override
    public Map<Class<?>, PrefReader<?>> readers() {
        return new ReaderMapBuilder()
                .add(Dimension.class, splitReader("x", s -> new Dimension(parseInt(s[0]), parseInt(s[1]))))
                .add(Point.class, splitReader(",", s -> new Point(parseInt(s[0]), parseInt(s[1]))))
                .add(Rectangle.class, splitReader(",", s -> new Rectangle(parseInt(s[0]), parseInt(s[1]), parseInt(s[2]), parseInt(s[3]))))
                .add(TextAttribute.class, stringReader(SwingPrefCodecs::stringToTextAttribute))
                .add(Font.class, mapReader(TextAttribute.class, String.class, SwingPrefCodecs::mapToFont))
                .build();
    }

    @Override
    public Map<Class<?>, PrefWriter<?>> writers() {
        return new WriterMapBuilder()
                .add(Dimension.class, (p, k, v) -> p.put(k, v.width + "x" + v.height))
                .add(Point.class, (p, k, v) -> p.put(k, v.x + "," + v.y))
                .add(Rectangle.class, (p, k, v) -> p.put(k, v.x + "," + v.y + "," + v.width + "," + v.height))
                .add(TextAttribute.class, stringWriter(SwingPrefCodecs::textAttributeToString))
                .add(Font.class, mapWriter(TextAttribute.class, String.class, SwingPrefCodecs::fillFont))
                .build();
    }

    private static String textAttributeToString(TextAttribute textAttribute) throws ReflectiveOperationException {
        for (final Field field : TextAttribute.class.getFields()) {
            if (field.getType() == TextAttribute.class && field.get(null) == textAttribute) {
                return field.getName();
            }
        }
        throw new IllegalArgumentException(textAttribute.toString());
    }

    private static TextAttribute stringToTextAttribute(String textAttribute) throws ReflectiveOperationException {
        for (final Field field : TextAttribute.class.getFields()) {
            if (field.getType() == TextAttribute.class && field.getName().equals(textAttribute)) {
                return (TextAttribute) field.get(null);
            }
        }
        throw new IllegalArgumentException(textAttribute);
    }

    private static Font mapToFont(Map<TextAttribute, String> map) {
        final Map<TextAttribute, Object> m = new LinkedHashMap<>();
        map.forEach((k, v) -> {
            if (k == TextAttribute.FAMILY) {
                m.put(k, v);
            } else if (k == TextAttribute.JUSTIFICATION) {
                m.put(k, Double.valueOf(v));
            } else if (k == TextAttribute.KERNING) {
                m.put(k, Integer.valueOf(v));
            } else if (k == TextAttribute.LIGATURES) {
                m.put(k, Integer.valueOf(v));
            } else if (k == TextAttribute.SIZE) {
                m.put(k, Float.valueOf(v));
            } else if (k == TextAttribute.WEIGHT) {
                m.put(k, Float.valueOf(v));
            } else if (k == TextAttribute.UNDERLINE) {
                m.put(k, Integer.valueOf(v));
            } else if (k == TextAttribute.STRIKETHROUGH) {
                m.put(k, Boolean.valueOf(v));
            } else if (k == TextAttribute.WIDTH) {
                m.put(k, Float.valueOf(v));
            }
        });
        return new Font(m);
    }

    private static void fillFont(Font font, Map<TextAttribute, String> map) {
        for (final Map.Entry<? extends TextAttribute, ?> e : font.getAttributes().entrySet()) {
            if (e.getKey() == TextAttribute.FAMILY) {
                map.put(e.getKey(), e.getValue().toString());
            } else if (e.getKey() == TextAttribute.JUSTIFICATION) {
                map.put(e.getKey(), e.getValue().toString());
            } else if (e.getKey() == TextAttribute.KERNING) {
                map.put(e.getKey(), e.getValue().toString());
            } else if (e.getKey() == TextAttribute.LIGATURES) {
                map.put(e.getKey(), e.getValue().toString());
            } else if (e.getKey() == TextAttribute.SIZE) {
                map.put(e.getKey(), e.getValue().toString());
            } else if (e.getKey() == TextAttribute.WEIGHT) {
                map.put(e.getKey(), e.getValue().toString());
            } else if (e.getKey() == TextAttribute.UNDERLINE) {
                map.put(e.getKey(), e.getValue().toString());
            } else if (e.getKey() == TextAttribute.STRIKETHROUGH) {
                map.put(e.getKey(), e.getValue().toString());
            } else if (e.getKey() == TextAttribute.WIDTH) {
                map.put(e.getKey(), e.getValue().toString());
            }
        }
    }

    public static <T> void addConsumer(Window window, Class<T> type, Preferences prefs, String key, Consumer<T> consumer) {
        final PreferenceChangeListener l = pce -> {
            if (key.equals(pce.getKey())) {
                consumer.accept(getReader(type).loadSafe(prefs, key));
            }
        };
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                prefs.addPreferenceChangeListener(l);
            }

            @Override
            public void windowClosed(WindowEvent e) {
                prefs.removePreferenceChangeListener(l);
                window.removeWindowListener(this);
            }
        });
    }

    public static <T> void addConsumer(JInternalFrame frame, Class<T> type, Preferences prefs, String key, Consumer<T> consumer) {
        final PreferenceChangeListener l = pce -> {
            if (key.equals(pce.getKey())) {
                consumer.accept(getReader(type).loadSafe(prefs, key));
            }
        };
        frame.addInternalFrameListener(new InternalFrameAdapter() {
            @Override
            public void internalFrameOpened(InternalFrameEvent e) {
                prefs.addPreferenceChangeListener(l);
            }

            @Override
            public void internalFrameClosed(InternalFrameEvent e) {
                prefs.removePreferenceChangeListener(l);
                frame.removeInternalFrameListener(this);
            }
        });
    }

    public static <T> void addConsumer(Component component, Class<T> type, Preferences prefs, String key, Consumer<T> consumer) {
        final Window window = SwingUtilities.windowForComponent(component);
        if (window != null) {
            addConsumer(window, type, prefs, key, consumer);
        } else {
            component.addHierarchyListener(new HierarchyListener() {
                @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if (e.getChanged() instanceof Window) {
                        addConsumer((Window) e.getChanged(), type, prefs, key, consumer);
                        component.removeHierarchyListener(this);
                    } else if (e.getChanged() instanceof JInternalFrame) {
                        addConsumer((JInternalFrame) e.getChanged(), type, prefs, key, consumer);
                        component.removeHierarchyListener(this);
                    }
                }
            });
        }
    }
}
