/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.logging;

import groovy.lang.Closure;
import org.marid.image.MaridIcons;
import org.marid.l10n.Localized;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.prefs.Preferences;

import static java.awt.RenderingHints.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingHandler extends AbstractHandler {

    private static final Map<Integer, ImageIcon> iconMap = new HashMap<>();

    private final EventListenerList listenerList = new EventListenerList();
    private final int size;
    private final LinkedList<LogRecord> queue = new LinkedList<>();

    public SwingHandler() throws Exception {
        String sizeText = manager.getProperty(getClass().getCanonicalName() + ".size");
        if (sizeText != null) {
            size = Integer.parseInt(sizeText);
        } else {
            size = 65536;
        }
        if (getFormatter() == null) {
            setFormatter(new SwingFormatter());
        }
    }

    @Override
    public void publish(final LogRecord record) {
        final LogRecordListModel[] models = listenerList.getListeners(LogRecordListModel.class);
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                while (queue.size() >= size) {
                    queue.remove();
                }
                queue.add(record);
                for (int i = models.length - 1; i >= 0; i--) {
                    models[i].add(record);
                }
            }
        });
    }

    public void show() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LogFrame().setVisible(true);
            }
        });
    }

    public ImageIcon getLevelIcon(Level level, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(0, 0, size, size);
        Color color;
        if (level.equals(Level.INFO)) {
            color = Color.BLUE;
        } else if (level.equals(Level.WARNING)) {
            color = Color.ORANGE;
        } else if (level.equals(Level.SEVERE)) {
            color = Color.RED;
        } else if (level.equals(Level.CONFIG)) {
            color = Color.GRAY;
        } else if (level.equals(Level.FINE)) {
            color = Color.CYAN;
        } else if (level.equals(Level.FINER)) {
            color = Color.MAGENTA;
        } else if (level.equals(Level.FINEST)) {
            color = Color.GREEN;
        } else {
            color = Color.WHITE;
        }
        g.setStroke(new BasicStroke(2.0f));
        g.setColor(color);
        g.fillOval(2, 2, size - 4, size - 4);
        g.setColor(SystemColor.controlShadow);
        g.drawOval(2, 2, size - 4, size - 4);
        return new ImageIcon(image);
    }

    private class LogFrame extends JFrame implements Localized {

        private final Preferences prefs = Preferences.userNodeForPackage(getClass());
        private final LogRecordList list;
        private final ButtonGroup levelGroup = new ButtonGroup();

        public LogFrame() {
            super(S.l("Marid log"));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setIconImages(MaridIcons.ICONS);
            setJMenuBar(new LogFrameMenu());
            add(new JScrollPane(list = new LogRecordList()));
            pack();
            int width = prefs.getInt("frameWidth", 400);
            int height = prefs.getInt("frameHeight", 500);
            setPreferredSize(new Dimension(width, height));
            pack();
            setLocationByPlatform(true);
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    listenerList.add(LogRecordListModel.class, list.getModel());
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    listenerList.remove(LogRecordListModel.class, list.getModel());
                }
            });
        }

        private void updateFilter() {
        }

        private class SelectLogLevelAction extends AbstractAction {

            private final Level level;

            public SelectLogLevelAction(Level level) {
                this.level = level;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                updateFilter();
            }
        }

        private class LogFrameMenu extends JMenuBar {

            public LogFrameMenu() {
                add(new JRadioButtonMenuItem());
            }
        }
    }

    private class LogRecordRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            LogRecord rec = (LogRecord) value;
            String val;
            try {
                val = getFormatter().format(rec);
            } catch (Exception x) {
                val = x.getMessage();
            }
            DefaultListCellRenderer r = (DefaultListCellRenderer)
                    super.getListCellRendererComponent(list, val, index, isSelected, cellHasFocus);
            ImageIcon icon = iconMap.get(rec.getLevel().intValue());
            if (icon == null) {
                int size = list.getFixedCellHeight() - 2;
                iconMap.put(rec.getLevel().intValue(), icon = getLevelIcon(rec.getLevel(), size));
            }
            r.setIcon(icon);
            return r;
        }
    }

    private class LogRecordList extends JList<LogRecord> {

        public LogRecordList() {
            super(new LogRecordListModel());
            if (getFont() != null) {
                setFont(new Font(Font.MONOSPACED, getFont().getStyle(), getFont().getSize()));
            }
            setCellRenderer(new LogRecordRenderer());
            setFixedCellHeight(20);
        }

        @Override
        public LogRecordListModel getModel() {
            return (LogRecordListModel) super.getModel();
        }
    }

    private class LogRecordListModel extends AbstractListModel<LogRecord> implements EventListener {

        private final ArrayList<LogRecord> records = new ArrayList<>(queue);
        private Closure filter;

        @Override
        public int getSize() {
            if (filter != null) {
                int count = 0;
                for (LogRecord record : records) {
                    try {
                        if (Boolean.TRUE.equals(filter.call(record))) {
                            count++;
                        }
                    } catch (Exception x) {
                        // Ignore to prevent stack overflow
                    }
                }
                return count;
            } else {
                return records.size();
            }
        }

        @Override
        public LogRecord getElementAt(int index) {
            if (filter != null) {
                int i = 0;
                for (LogRecord record : records) {
                    try {
                        if (Boolean.TRUE.equals(filter.call(record)) && i++ == index) {
                            return record;
                        }
                    } catch (Exception x) {
                        // Ignore it to prevent stack overflow
                    }
                }
                return null;
            } else {
                return records.get(index);
            }
        }

        public void setFilter(Closure filter) {
            this.filter = filter;
            fireContentsChanged(this, 0, getSize() - 1);
        }

        public void add(LogRecord record) {
            records.add(record);
            if (filter != null) {
                try {
                    if (Boolean.TRUE.equals(filter.call(record))) {
                        int size = getSize();
                        fireIntervalAdded(this, size - 1, size - 1);
                    }
                } catch (Exception x) {
                    // Simple ignoring
                }
            } else {
                fireIntervalAdded(this, records.size() - 1, records.size() - 1);
            }
        }
    }
}
