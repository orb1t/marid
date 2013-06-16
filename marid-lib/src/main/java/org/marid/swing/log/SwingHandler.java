package org.marid.swing.log;

import org.marid.image.MaridIcons;
import org.marid.l10n.Localized;
import org.marid.logging.AbstractHandler;
import org.marid.logging.Logging;
import org.marid.swing.MaridAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.prefs.Preferences;

import static java.awt.RenderingHints.*;
import static org.marid.methods.GuiMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingHandler extends AbstractHandler implements Localized {

    private final int size;
    private final LinkedList<LogRecord> queue = new LinkedList<>();
    private final LinkedList<LogRecordListModel> models = new LinkedList<>();
    private static final Map<Level, ImageIcon> iconMap = new HashMap<>();

    public SwingHandler() throws Exception {
        String sizeText = manager.getProperty(getClass().getCanonicalName() + ".size");
        size = sizeText == null ? 65536 : Integer.parseInt(sizeText);
        if (getFormatter() == null) {
            setFormatter(new SwingHandlerFormatter());
        }
    }

    @Override
    public void publish(final LogRecord record) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                while (queue.size() >= size) {
                    queue.remove();
                }
                queue.add(record);
                for (LogRecordListModel model : models) {
                    model.add(record);
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

    static Color getLevelColor(Level level) {
        if (level.intValue() == Level.INFO.intValue()) {
            return Color.BLUE;
        } else if (level.intValue() == Level.WARNING.intValue()) {
            return Color.ORANGE;
        } else if (level.intValue() == Level.FINE.intValue()) {
            return Color.CYAN;
        } else if (level.intValue() == Level.FINER.intValue()) {
            return Color.MAGENTA;
        } else if (level.intValue() == Level.SEVERE.intValue()) {
            return Color.RED;
        } else if (level.intValue() == Level.FINEST.intValue()) {
            return Color.GREEN;
        } else if (level.intValue() == Level.CONFIG.intValue()) {
            return Color.GRAY;
        } else {
            return Color.WHITE;
        }
    }

    static ImageIcon getLevelIcon(Level level, int size) {
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g.setRenderingHint(KEY_TEXT_ANTIALIASING, VALUE_TEXT_ANTIALIAS_ON);
        g.setBackground(new Color(0, 0, 0, 0));
        g.clearRect(0, 0, size, size);
        g.setStroke(new BasicStroke(2.0f));
        g.setColor(getLevelColor(level));
        g.fillOval(2, 2, size - 4, size - 4);
        g.setColor(SystemColor.controlShadow);
        g.drawOval(2, 2, size - 4, size - 4);
        return new ImageIcon(image);
    }

    @SuppressWarnings("serial")
    class LogRecordRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(
                JList list, Object v, int i, boolean s, boolean f) {
            LogRecord rec = (LogRecord) v;
            String val;
            try {
                val = getFormatter().format(rec);
            } catch (Exception x) {
                val = x.getMessage();
            }
            DefaultListCellRenderer r = (DefaultListCellRenderer)
                    super.getListCellRendererComponent(list, val, i, s, f);
            ImageIcon icon = iconMap.get(rec.getLevel());
            if (icon == null) {
                int size = list.getFixedCellHeight() - 2;
                iconMap.put(rec.getLevel(), icon = getLevelIcon(rec.getLevel(), size));
            }
            r.setIcon(icon);
            return r;
        }
    }

    @SuppressWarnings("serial")
    private class LogFrame extends JFrame implements Comparator<Level>, Filter {

        private final Preferences prefs = preferences("logFrame");
        private final TreeMap<Level, Action> levelMap = new TreeMap<>(this);
        private final LogRecordListModel model;
        private Filter filter;

        public LogFrame() {
            super(S.l("Marid log"));
            this.model = new LogRecordListModel();
            for (Level level : Logging.LEVELS) {
                levelMap.put(level, new LevelAction(level));
            }
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setIconImages(MaridIcons.ICONS);
            setPreferredSize(getDimension(prefs, "size", new Dimension(300, 400)));
            JList<LogRecord> list = new JList<>(model);
            list.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            list.setFixedCellHeight(20);
            list.setCellRenderer(new LogRecordRenderer());
            add(new JScrollPane(list));
            setJMenuBar(new LogFrameMenu());
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    models.add(model);
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    models.remove(model);
                    if ((getExtendedState() & MAXIMIZED_BOTH) == 0) {
                        putDimension(prefs, "size", getSize());
                    }
                    levelMap.clear();
                }
            });
            pack();
            setLocationByPlatform(true);
        }

        @Override
        public int compare(Level o1, Level o2) {
            return Integer.compare(o1.intValue(), o2.intValue());
        }

        @Override
        public boolean isLoggable(LogRecord record) {
            Action action = levelMap.get(record.getLevel());
            return action == null || Boolean.TRUE.equals(action.getValue(Action.SELECTED_KEY));
        }

        private boolean selected() {
            for (Action a : levelMap.values()) {
                if (Boolean.TRUE.equals(a.getValue(Action.SELECTED_KEY))) {
                    return true;
                }
            }
            return false;
        }

        void updateFilter() {
            if (selected()) {
                model.setFilter(this);
            } else if (filter != null) {
                model.setFilter(filter);
            } else {
                model.setFilter(null);
            }
        }

        private class LogFrameMenu extends JMenuBar {

            public LogFrameMenu() {
                JMenu filterMenu = new JMenu(S.l("Filter"));
                for (Action a : levelMap.values()) {
                    filterMenu.add(new JCheckBoxMenuItem(a));
                }
                filterMenu.addSeparator();
                filterMenu.add(new MaridAction("Clear filter", "clear.png") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        for (Action a : levelMap.values()) {
                            a.putValue(SELECTED_KEY, false);
                        }
                    }
                });
                filterMenu.addSeparator();
                add(filterMenu);
            }
        }

        private class LevelAction extends AbstractAction implements PropertyChangeListener {

            public LevelAction(Level level) {
                super(level.getLocalizedName(), getLevelIcon(level, 16));
                putValue(SELECTED_KEY, false);
                addPropertyChangeListener(this);
            }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (SELECTED_KEY.equals(evt.getPropertyName())) {
                    updateFilter();
                }
            }

            @Override
            public void actionPerformed(ActionEvent e) {
            }
        }
    }

    @SuppressWarnings("serial")
    private class LogRecordListModel extends AbstractListModel<LogRecord> {

        private final ArrayList<LogRecord> records;
        private Filter filter;

        LogRecordListModel() {
            records = new ArrayList<>(queue);
        }

        @Override
        public int getSize() {
            if (filter != null) {
                int count = 0;
                for (LogRecord record : records) {
                    try {
                        if (filter.isLoggable(record)) {
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
                        if (filter.isLoggable(record) && i++ == index) {
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

        void setFilter(Filter filter) {
            this.filter = filter;
            fireContentsChanged(this, 0, getSize() - 1);
        }

        void add(LogRecord record) {
            records.add(record);
            if (filter != null) {
                try {
                    if (filter.isLoggable(record)) {
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
