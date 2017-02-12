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

package org.marid.db.generator.swing;

import org.marid.db.dao.NumericWriter;
import org.marid.db.data.DataRecord;
import org.marid.db.generator.swing.SwingNumericDaqGeneratorModel.TagInfo;
import org.marid.logging.LogSupport;
import org.marid.xml.XmlBind;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableColumn;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
class SwingNumericDaqGeneratorFrame extends JFrame implements LogSupport {

    private final SwingNumericDaqGeneratorModel model = new SwingNumericDaqGeneratorModel();
    private final JTable table = new JTable(model);
    private final JSpinner periodSpinner = new JSpinner(new SpinnerNumberModel(1000, 100, 60000, 100));
    private final Map<String, Action> actionMap = new HashMap<>();
    private final Timer timer;

    SwingNumericDaqGeneratorFrame(NumericWriter numericWriter) {
        super(s("Swing numeric DAQ generator"));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setJMenuBar(new JMenuBar());
        setPreferredSize(new Dimension(800, 600));
        add(new JScrollPane(table, VERTICAL_SCROLLBAR_AS_NEEDED, HORIZONTAL_SCROLLBAR_NEVER));
        add(toolBar(), BorderLayout.NORTH);
        pack();
        setLocationRelativeTo(null);
        initColumns();
        timer = new Timer((int) periodSpinner.getValue(), e -> {
            final ArrayList<DataRecord<Double>> records = new ArrayList<>();
            model.visitTagInfos(tagInfo -> records.add(new DataRecord<>(tagInfo.tag, System.currentTimeMillis(), (double) tagInfo.value)));
            records.forEach(record -> log(INFO, "Generated {0}", record));
            numericWriter.merge(records, true);
        });
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                timer.stop();
            }
        });
        periodSpinner.addChangeListener(e -> timer.setDelay((int) periodSpinner.getValue()));
    }

    private void initColumns() {
        table.setRowHeight(48);
        table.setShowGrid(true);
        table.setGridColor(SystemColor.control);
        final int[] widths = {100, 50, 50, 50, 300};
        for (int i = 0; i < widths.length; i++) {
            final TableColumn column = table.getColumnModel().getColumn(i);
            column.setMinWidth(widths[i] / 2);
            column.setMaxWidth(widths[i] * 5);
            column.setPreferredWidth(widths[i]);
        }
        final TableColumn lastColumn = table.getColumnModel().getColumn(4);
        lastColumn.setCellEditor(new SliderTableCellEditor());
        lastColumn.setCellRenderer(new SliderTableCellRenderer());
    }

    private JToolBar toolBar() {
        final JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        add(toolBar, "Add tag", "awicons/vista-artistic/24/add-icon.png", e -> {
            final long maxTag = model.maxTag();
            final TagInfo tagInfo = new TagInfo();
            tagInfo.tag = maxTag + 1L;
            model.add(tagInfo);
        });
        toolBar.addSeparator();
        add(toolBar, "Load", "oxygen-icons.org/oxygen/24/Actions-document-open-icon.png", e -> {
            final JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("XML", "xml"));
            chooser.setMultiSelectionEnabled(false);
            switch (chooser.showOpenDialog(this)) {
                case JFileChooser.APPROVE_OPTION:
                    final File file = chooser.getSelectedFile();
                    model.load(XmlBind.load(SwingNumericDaqGeneratorModel.class, file, Unmarshaller::unmarshal));
                    break;
            }
        });
        add(toolBar, "Save", "oxygen-icons.org/oxygen/24/Actions-document-save-icon.png", e -> {
            final JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("XML", "xml"));
            switch (chooser.showOpenDialog(this)) {
                case JFileChooser.APPROVE_OPTION:
                    if (!chooser.getSelectedFile().getName().endsWith(".xml")) {
                        final File file = chooser.getSelectedFile();
                        chooser.setSelectedFile(new File(file.getParentFile(), file.getName() + ".xml"));
                    }
                    XmlBind.save(model, chooser.getSelectedFile(), Marshaller::marshal);
                    break;
            }
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

    private static ImageIcon icon(String url) {
        try {
            return new ImageIcon(new URL(url));
        } catch (Exception x) {
            return null;
        }
    }

}
