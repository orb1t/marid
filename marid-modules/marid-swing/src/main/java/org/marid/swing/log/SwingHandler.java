package org.marid.swing.log;

import org.marid.image.MaridIcons;
import org.marid.logging.AbstractHandler;
import org.marid.logging.Logging;
import org.marid.pref.PrefSupport;
import org.marid.swing.MaridAction;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

import static java.awt.RenderingHints.*;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingHandler extends AbstractHandler implements PrefSupport {

    private static final Map<Level, ImageIcon> iconMap = new HashMap<>();
    private final int size;
    private final LinkedList<LogRecord> queue = new LinkedList<>();
    private final ConcurrentLinkedQueue<LogRecordListModel> models = new ConcurrentLinkedQueue<>();

    public SwingHandler() throws Exception {
        final String sizeText = LogManager.getLogManager().getProperty(getClass().getCanonicalName() + ".size");
        size = sizeText == null ? 65536 : Integer.parseInt(sizeText);
        if (getFormatter() == null) {
            setFormatter(new SwingHandlerFormatter());
        }
    }

    @Override
    public void publish(final LogRecord record) {
        if (record.getSourceMethodName() != null) {
            return;
        }
        synchronized (queue) {
            queue.add(record);
            if (queue.size() > size) {
                queue.removeFirst();
            }
        }
        for (final LogRecordListModel model : models) {
            model.buffer.add(record);
        }
    }

    public void show() {
        EventQueue.invokeLater(() -> {
            new LogFrame().setVisible(true);
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
        final BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        final Graphics2D g = image.createGraphics();
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

    class LogRecordRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object v, int i, boolean s, boolean f) {
            final LogRecord rec = (LogRecord) v;
            String val;
            try {
                val = getFormatter().format(rec);
            } catch (Exception x) {
                val = x.getMessage();
            }
            final JLabel r = (DefaultListCellRenderer) super.getListCellRendererComponent(list, val, i, s, f);
            ImageIcon icon = iconMap.get(rec.getLevel());
            if (icon == null) {
                int size = list.getFixedCellHeight() - 2;
                iconMap.put(rec.getLevel(), icon = getLevelIcon(rec.getLevel(), size));
            }
            r.setIcon(icon);
            return r;
        }
    }

    private class LogFrame extends JFrame implements Filter {

        private final TreeMap<Level, Action> levelMap = new TreeMap<>((l1, l2) -> l1.intValue() - l2.intValue());
        private final LogRecordListModel model = new LogRecordListModel();
        private final Timer timer = new Timer(100, e -> {
            final List<LogRecord> records = new LinkedList<>();
            for (final Iterator<LogRecord> it = model.buffer.iterator(); it.hasNext(); ) {
                final LogRecord r = it.next();
                it.remove();
                records.add(r);
            }
            model.add(records);
        });
        private Filter filter;

        public LogFrame() {
            super(s("Marid log"));
            setAlwaysOnTop(getPref("alwaysOnTop", true));
            for (Level level : Logging.LEVELS) {
                levelMap.put(level, new LevelAction(level));
            }
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setIconImages(MaridIcons.ICONS);
            setPreferredSize(getPref("size", new Dimension(300, 400)));
            final JList<LogRecord> list = new JList<>(model);
            list.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            list.setFixedCellHeight(20);
            list.setCellRenderer(new LogRecordRenderer());
            add(new JScrollPane(list));
            setJMenuBar(new LogFrameMenu());
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    timer.start();
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    timer.stop();
                    models.remove(model);
                    if ((getExtendedState() & MAXIMIZED_BOTH) == 0) {
                        putPref("size", getSize());
                    }
                    levelMap.clear();
                }
            });
            pack();
            setLocationByPlatform(true);
        }

        @Override
        public boolean isLoggable(LogRecord record) {
            final Action action = levelMap.get(record.getLevel());
            return action == null || Boolean.TRUE.equals(action.getValue(Action.SELECTED_KEY));
        }

        void updateFilter() {
            if (levelMap.values().stream().anyMatch(a -> Boolean.TRUE.equals(a.getValue(Action.SELECTED_KEY)))) {
                model.setFilter(this);
            } else if (filter != null) {
                model.setFilter(filter);
            } else {
                model.setFilter(null);
            }
        }

        private class LogFrameMenu extends JMenuBar {

            public LogFrameMenu() {
                final JMenu filterMenu = new JMenu(s("Filter"));
                levelMap.values().forEach(a -> filterMenu.add(new JCheckBoxMenuItem(a)));
                filterMenu.addSeparator();
                filterMenu.add(new MaridAction("Clear filter", "clear.png", (a, e) ->
                        levelMap.values().forEach(ac -> ac.putValue(Action.SELECTED_KEY, false))));
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

    private class LogRecordListModel extends AbstractListModel<LogRecord> {

        private final ConcurrentLinkedQueue<LogRecord> buffer = new ConcurrentLinkedQueue<>();
        private final ArrayList<LogRecord> records;
        private int limit = Integer.MAX_VALUE;
        private Filter filter;

        LogRecordListModel() {
            synchronized (queue) {
                records = new ArrayList<>(queue);
                models.add(this);
            }
        }

        @Override
        public int getSize() {
            if (filter != null) {
                int count = 0;
                for (final LogRecord record : records) {
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
                for (final LogRecord record : records) {
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

        // TODO: implement limit support
        void setLimit(int limit) {
            this.limit = limit;
        }

        void add(List<LogRecord> records) {
            final int initCount = this.records.size();
            this.records.addAll(records);
            if (filter != null) {
                final int count = (int) records.stream().filter(filter::isLoggable).count();
                fireIntervalAdded(this, initCount, initCount + count);
            } else {
                fireIntervalAdded(this, initCount, this.records.size());
            }
        }
    }
}
