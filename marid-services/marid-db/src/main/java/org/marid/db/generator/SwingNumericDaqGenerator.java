/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.db.generator;

import org.marid.db.dao.NumericWriter;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;

import javax.swing.*;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.awt.GridBagConstraints.*;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingNumericDaqGenerator implements LogSupport {

    private final NumericWriter numericWriter;

    public SwingNumericDaqGenerator(NumericWriter numericWriter) {
        this.numericWriter = numericWriter;
    }

    @Autowired
    private void init(ConfigurableApplicationContext context) throws UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(new NimbusLookAndFeel());
        EventQueue.invokeLater(() -> {
            final Frame frame = new Frame();
            context.addApplicationListener(event -> {
                if (event instanceof ContextClosedEvent) {
                    final ContextClosedEvent e = (ContextClosedEvent) event;
                    if (e.getApplicationContext() == context) {
                        frame.dispose();
                    }
                }
            });
            frame.setVisible(true);
        });
    }

    private static ImageIcon icon(String url) {
        try {
            return new ImageIcon(new URL(url));
        } catch (Exception x) {
            return null;
        }
    }

    private class Frame extends JFrame {

        private final GridBagLayout layout = new GridBagLayout();
        private final GridBagConstraints constraints = new GridBagConstraints(
                REMAINDER, RELATIVE, 1, 1, 1.0, 0.0, BASELINE, HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0);
        private final JPanel panel = new JPanel();
        private final JSpinner periodSpinner = new JSpinner(new SpinnerNumberModel(1000, 100, 60000, 100));
        private final Map<String, Action> actionMap = new HashMap<>();
        private final Timer timer = new Timer((int) periodSpinner.getValue(), e -> {

        });

        private Frame() {
            super(s("Swing numeric DAQ generator"));
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            setJMenuBar(new JMenuBar());
            setPreferredSize(new Dimension(800, 600));
            panel.setLayout(layout);
            add(new JScrollPane(panel, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
            add(toolBar(), BorderLayout.NORTH);
            pack();
            setLocationRelativeTo(null);
            periodSpinner.addChangeListener(e -> timer.setDelay((int) periodSpinner.getValue()));
        }

        private JToolBar toolBar() {
            final JToolBar toolBar = new JToolBar();
            toolBar.setFloatable(false);
            add(toolBar, "Add tag", "awicons/vista-artistic/24/add-icon.png", e -> {
                panel.add(new TagPanel(), constraints);
                panel.validate();
                panel.getParent().validate();
            });
            toolBar.addSeparator();
            add(toolBar, "Load", "oxygen-icons.org/oxygen/24/Actions-document-open-icon.png", e -> {
            });
            add(toolBar, "Save", "oxygen-icons.org/oxygen/24/Actions-document-save-icon.png", e -> {
            });
            toolBar.addSeparator();
            add(toolBar, "Run", "oxygen-icons.org/oxygen/24/Actions-media-playback-start-icon.png", e -> {
                actionMap.get("Run").setEnabled(false);
                actionMap.get("Stop").setEnabled(true);
                timer.start();
            });
            add(toolBar, "Stop", "oxygen-icons.org/oxygen/24/Actions-media-playback-stop-icon.png", e -> {
                actionMap.get("Run").setEnabled(true);
                actionMap.get("Stop").setEnabled(false);
                timer.stop();
            }).getAction().setEnabled(false);
            toolBar.addSeparator();
            toolBar.add(periodSpinner);
            Stream.of(toolBar.getComponents()).forEach(c -> c.setFocusable(false));
            return toolBar;
        }

        private JButton add(JToolBar toolBar, String label, String path, ActionListener listener) {
            final String text = s(label);
            final AbstractAction action = new AbstractAction(text, icon("http://icons.iconarchive.com/icons/" + path)) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listener.actionPerformed(e);
                }
            };
            actionMap.put(label, action);
            action.putValue(Action.SHORT_DESCRIPTION, text);
            return toolBar.add(action);
        }
    }

    private class TagPanel extends JPanel {

        private final JTextField max = new JTextField("1.0");
        private final JTextField min = new JTextField("0.0");
        private final JTextField tick = new JTextField("0.01");

        private TagPanel() {
            super(new FlowLayout());
            add(new JLabel(s("Min") + ": "));
            add(min);
            add(new JLabel(s("Max") + ": "));
            add(max);
            add(new JLabel(s("Tick") + ": "));
            add(tick);
        }
    }
}