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

package org.marid.swing.log;

import org.marid.l10n.L10nSupport;
import org.marid.logging.LogLevel;
import org.marid.swing.control.ToolbarUtil;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.prefs.Preferences;

import static java.awt.RenderingHints.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class LogComponent extends JPanel implements L10nSupport {

    protected final Preferences preferences;
    protected final JToolBar toolBar;
    protected final Model model;
    protected final JTable table;
    protected final Filter recordFilter;

    public LogComponent(Preferences prefs, Collection<LogRecord> logRecords, Filter filter) {
        super(new BorderLayout());
        recordFilter = filter;
        preferences = prefs;
        toolBar = addToolBar();
        model = model(logRecords);
        add(new JScrollPane(this.table = new JTable(model)));
        table.setRowHeight(20);
        table.setFont(new Font(Font.MONOSPACED, Font.PLAIN, UIManager.getFont("Label.font").getSize() - 2));
        table.setShowVerticalLines(true);
        table.setGridColor(SystemColor.control);
        for (int i = 0; i < table.getColumnCount(); i++) {
            final int width = model.getColumnWidth(i);
            if (width > 0) {
                table.getColumnModel().getColumn(i).setMaxWidth(width * 3 / 2);
                table.getColumnModel().getColumn(i).setPreferredWidth(width);
                table.getColumnModel().getColumn(i).setMinWidth(width / 2);
            }
        }
        if (table.getRowCount() > 0) {
            table.getSelectionModel().setSelectionInterval(table.getRowCount() - 1, table.getRowCount() - 1);
            track();
        }
    }

    private JToolBar addToolBar() {
        final String side = preferences.get("toolbarPos", BorderLayout.NORTH);
        final JToolBar toolBar;
        add(toolBar = new JToolBar(ToolbarUtil.getToolbarOrientation(side)), side);
        return toolBar;
    }

    protected Model model(Collection<LogRecord> logRecords) {
        return new Model(logRecords);
    }

    protected boolean isTracking() {
        return table.getSelectedRow() == table.getRowCount() - 1;
    }

    protected void track() {
        table.scrollRectToVisible(table.getCellRect(table.getSelectedRow(), 0, true));
    }

    public void publish(LogRecord record) {
        if (recordFilter.isLoggable(record)) {
            EventQueue.invokeLater(() -> {
                final boolean tracking = isTracking();
                model.add(record);
                if (tracking) {
                    final int index = table.getRowCount() - 1;
                    table.getSelectionModel().setSelectionInterval(index, index);
                    track();
                }
            });
        }
    }

    protected class Model extends AbstractTableModel {

        protected final Map<LogLevel, Color> colorMap = new EnumMap<>(LogLevel.class);
        protected final Map<Level, ImageIcon> iconMap = new HashMap<>();
        protected final SwingHandlerFormatter formatter = new SwingHandlerFormatter();
        protected final List<LogRecord> records;

        private DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
        private Filter filter = r -> true;

        public Model(Collection<LogRecord> logRecords) {
            this.records = new ArrayList<>(logRecords);
            colorMap.put(LogLevel.INFO, Color.BLUE);
            colorMap.put(LogLevel.WARNING, Color.ORANGE);
            colorMap.put(LogLevel.SEVERE, Color.RED);
            colorMap.put(LogLevel.FINE, Color.CYAN);
            colorMap.put(LogLevel.FINER, Color.MAGENTA);
            colorMap.put(LogLevel.FINEST, Color.GREEN);
            colorMap.put(LogLevel.CONFIG, Color.GRAY);
            colorMap.put(LogLevel.OFF, Color.BLACK);
            colorMap.put(LogLevel.ALL, Color.WHITE);
        }

        public void add(LogRecord record) {
            records.add(record);
            if (filter.isLoggable(record)) {
                final int index = getRowCount() - 1;
                fireTableRowsInserted(index, index);
            }
            while (records.size() > preferences.getInt("maxRecords", 16384)) {
                final LogRecord removed = records.remove(0);
                if (filter.isLoggable(removed)) {
                    fireTableRowsDeleted(0, 0);
                }
            }
        }

        @Override
        public int getRowCount() {
            return (int) records.stream().filter(filter::isLoggable).count();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        public int getColumnWidth(int column) {
            switch (column) {
                case 0:
                    return 50;
                case 1:
                    return 300;
                case 2:
                    return 100;
                default:
                    return -1;
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0:
                    return s("Type");
                case 1:
                    return s("Logger");
                case 2:
                    return s("Time");
                case 3:
                    return s("Message");
                default:
                    return "";
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return ImageIcon.class;
                case 1:
                    return String.class;
                case 2:
                    return ZonedDateTime.class;
                case 3:
                    return String.class;
                default:
                    return Object.class;
            }
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            final LogRecord r = records.stream().filter(filter::isLoggable).skip(rowIndex).findFirst().orElse(null);
            if (r == null) {
                return null;
            } else {
                switch (columnIndex) {
                    case 0:
                        return getLevelIcon(r.getLevel(), 18);
                    case 1:
                        return r.getLoggerName();
                    case 2:
                        return Instant.ofEpochMilli(r.getMillis()).atZone(ZoneId.systemDefault()).format(timeFormatter);
                    case 3:
                        return formatter.format(r);
                    default:
                        return null;
                }
            }
        }

        protected ImageIcon getLevelIcon(Level level, int size) {
            return iconMap.compute(level, (l, ic) -> {
                if (ic != null && ic.getIconWidth() == size) {
                    return ic;
                } else {
                    final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                    final Graphics2D g = image.createGraphics();
                    g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
                    g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
                    g.setBackground(new Color(0, 0, 0, 0));
                    g.clearRect(0, 0, size, size);
                    g.setStroke(new BasicStroke(2.0f));
                    g.setColor(colorMap.get(LogLevel.findBy(l)));
                    g.fillOval(2, 2, size - 4, size - 4);
                    g.setColor(SystemColor.controlShadow);
                    g.drawOval(2, 2, size - 4, size - 4);
                    return new ImageIcon(image);
                }
            });
        }
    }
}
