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

package org.marid.bd.schema;

import groovy.inspect.swingui.AstNodeToScriptVisitor;
import images.Images;
import org.codehaus.groovy.ast.ClassNode;
import org.marid.bd.BlockComponent;
import org.marid.bd.shapes.LinkShape;
import org.marid.bd.shapes.LinkShapeEvent;
import org.marid.ide.components.BlockMenuProvider;
import org.marid.ide.components.BlockPersister;
import org.marid.ide.components.ProfileManager;
import org.marid.ide.profile.Profile;
import org.marid.swing.AbstractFrame;
import org.marid.swing.SwingUtil;
import org.marid.swing.menu.MenuActionList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.LayerUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;

import static java.awt.Color.RED;
import static java.awt.SystemColor.infoText;
import static java.lang.String.format;
import static javax.swing.BorderFactory.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SchemaFrame extends AbstractFrame implements SchemaFrameConfiguration {

    protected final ProfileManager profileManager;
    protected final BlockPersister persister;
    protected final SchemaEditor schemaEditor = new SchemaEditor(this);
    protected final JLayer<SchemaEditor> layer = new JLayer<>(schemaEditor, new SchemaEditorLayerUI());
    protected final JMenu blocksMenu = new JMenu(s("Blocks"));
    protected File file;

    @Autowired
    public SchemaFrame(BlockMenuProvider blockMenuProvider,
                       ProfileManager profileManager,
                       BlockPersister persister,
                       AutowireCapableBeanFactory autowireCapableBeanFactory) {
        super("Schema");
        this.profileManager = profileManager;
        this.persister = persister;
        autowireCapableBeanFactory.autowireBean(schemaEditor);
        enableEvents(AWTEvent.COMPONENT_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
        centerPanel.add(layer);
        getContentPane().setBackground(getBackground());
        getJMenuBar().add(blocksMenu);
        blockMenuProvider.fillMenu(blocksMenu);
        pack();
    }

    protected void fireEvent(AWTEvent event) {
        layer.getUI().eventDispatched(event, layer);
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        switch (e.getID()) {
            case WindowEvent.WINDOW_OPENED:
                schemaEditor.start();
                break;
            case WindowEvent.WINDOW_CLOSED:
                schemaEditor.stop();
                break;
        }
    }

    @Override
    protected void fillActions(MenuActionList actionList) {
        actionList.add("main", "File");
        actionList.add(true, "open", "Open...", "File")
                .setKey("control O")
                .setIcon("open")
                .setListener(this::open);
        actionList.add(true, "save", "Save", "File")
                .setKey("control S")
                .setIcon("save")
                .setListener(this::save);
        actionList.add("save", "Save As...", "File")
                .setKey("control shift S")
                .setIcon("save")
                .setListener(this::saveAs);
        actionList.add("main", "Schema");
        actionList.add(true, "zoom", "Zoom in", "Schema")
                .setKey("control I")
                .setIcon("zoomin")
                .setListener(e -> schemaEditor.zoomIn());
        actionList.add(true, "zoom", "Zoom out", "Schema")
                .setKey("control I")
                .setIcon("zoomout")
                .setListener(e -> schemaEditor.zoomOut());
        actionList.add(true, "zoom", "Reset zoom", "Schema")
                .setKey("control R")
                .setIcon("zoom")
                .setListener(e -> schemaEditor.resetZoom());
    }

    protected void open(ActionEvent actionEvent) {
        final Profile profile = profileManager.getCurrentProfile();
        final File dir = profile == null ? new File(".") : profile.getContextPath().toFile();
        final JFileChooser chooser = new JFileChooser(dir);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("XML files", "xml"));
        chooser.setMultiSelectionEnabled(false);
        switch (chooser.showOpenDialog(this)) {
            case JFileChooser.APPROVE_OPTION:
                try {
                    final SchemaModel model = persister.load(chooser.getSelectedFile().toPath());
                    schemaEditor.load(model);
                    file = chooser.getSelectedFile();
                } catch (Exception x) {
                    showMessage(ERROR_MESSAGE, "Load error", "Load {0} error", x, chooser.getSelectedFile());
                }
                break;
        }
    }

    protected void save(ActionEvent actionEvent) {
        if (file == null) {
            saveAs(actionEvent);
            return;
        }
        try {
            final SchemaModel model = new SchemaModel(schemaEditor);
            persister.save(model, file.toPath());
            final List<ClassNode> classNodes = SchemaToCode.schemaToCode(model.getSchema());
            final Profile profile = profileManager.getCurrentProfile();
            for (final ClassNode classNode : classNodes) {
                final String name = classNode.getNameWithoutPackage() + ".groovy";
                try (final Writer writer = Files.newBufferedWriter(profile.getClassesPath().resolve(name))) {
                    final AstNodeToScriptVisitor astNodeToScriptVisitor = new AstNodeToScriptVisitor(writer);
                    astNodeToScriptVisitor.visitClass(classNode);
                }
            }
        } catch (Exception x) {
            showMessage(ERROR_MESSAGE, "Save error", "Save {0} error", x, file);
        }
    }

    protected void saveAs(ActionEvent actionEvent) {
        final Profile profile = profileManager.getCurrentProfile();
        final File dir = profile == null ? new File(".") : profile.getContextPath().toFile();
        final JFileChooser chooser = new JFileChooser(dir);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("XML files", "xml"));
        chooser.setMultiSelectionEnabled(false);
        switch (chooser.showSaveDialog(this)) {
            case JFileChooser.APPROVE_OPTION:
                file = chooser.getSelectedFile();
                save(actionEvent);
                break;
        }
    }

    protected class SchemaEditorLayerUI extends LayerUI<SchemaEditor> {

        private JLabel tooltip = null;

        @Override
        public void paint(Graphics g, JComponent c) {
            super.paint(g, c);
            if (tooltip != null) {
                final Rectangle bounds = tooltip.getBounds();
                if (bounds.getMaxX() > schemaEditor.getWidth()) {
                    tooltip.setLocation(schemaEditor.getWidth() - bounds.width, tooltip.getY());
                }
                if (bounds.getMaxY() > schemaEditor.getHeight()) {
                    tooltip.setLocation(tooltip.getX(), schemaEditor.getHeight() - bounds.height);
                }
                try {
                    g.translate(tooltip.getX(), tooltip.getY());
                    tooltip.print(g);
                } finally {
                    g.translate(-tooltip.getX(), -tooltip.getY());
                }
            }
        }

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            ((JLayer) c).setLayerEventMask(AWTEvent.MOUSE_EVENT_MASK);
        }

        @Override
        public void uninstallUI(JComponent c) {
            super.uninstallUI(c);
            ((JLayer) c).setLayerEventMask(0L);
        }

        @Override
        public void eventDispatched(AWTEvent e, JLayer<? extends SchemaEditor> l) {
            super.eventDispatched(e, l);
            switch (e.getID()) {
                case MouseEvent.MOUSE_ENTERED:
                    if (e.getSource() instanceof BlockComponent.Input) {
                        final BlockComponent.Input input = (BlockComponent.Input) e.getSource();
                        final Point point = ((MouseEvent) e).getLocationOnScreen();
                        SwingUtilities.convertPointFromScreen(point, schemaEditor);
                        showTooltip(format("%s: %s", input.getInput().getName(), input.getInput().getInputType().getCanonicalName()), point);
                    } else if (e.getSource() instanceof BlockComponent.Output) {
                        final BlockComponent.Output output = (BlockComponent.Output) e.getSource();
                        final Point point = ((MouseEvent) e).getLocationOnScreen();
                        SwingUtilities.convertPointFromScreen(point, schemaEditor);
                        showTooltip(format("%s: %s", output.getOutput().getName(), output.getOutput().getOutputType().getCanonicalName()), point);
                    }
                    break;
                case MouseEvent.MOUSE_EXITED:
                    if (e.getSource() instanceof BlockComponent.Input || e.getSource() instanceof BlockComponent.Output) {
                        hideTooltip();
                    }
                    break;
                default:
                    if (e instanceof LinkShapeEvent) {
                        switch (e.getID()) {
                            case LinkShapeEvent.MOUSE_ENTERED:
                                final LinkShapeEvent event = (LinkShapeEvent) e;
                                final LinkShape link = event.getSource();
                                if (!link.isValid()) {
                                    showError(m("Types mismatch: {0} -> {1}",
                                            link.getOutputType().getCanonicalName(),
                                            link.getInputType().getCanonicalName()), event.getPoint());
                                }
                                break;
                            case LinkShapeEvent.MOUSE_EXITED:
                                hideTooltip();
                                break;
                        }
                    }
                    break;
            }
        }

        public void hideTooltip() {
            if (tooltip != null) {
                tooltip = null;
                layer.repaint();
            }
        }

        public void showTooltip(String text, Point point) {
            showMessage(Images.getIcon("info.png"), SystemColor.info, text, point);
        }

        public void showError(String text, Point point) {
            showMessage(Images.getIcon("warning.png"), RED.brighter().brighter(), text, point);
        }

        public void showMessage(ImageIcon icon, Color color, String text, Point point) {
            final JLabel label = new JLabel(text, icon, SwingConstants.LEFT);
            label.setOpaque(true);
            label.setForeground(infoText);
            label.setBackground(SwingUtil.color(color, 200));
            label.setBorder(createCompoundBorder(createEtchedBorder(), createEmptyBorder(3, 3, 3, 3)));
            label.setLocation(point.x + 10, point.y + 10);
            label.setSize(label.getPreferredSize());
            tooltip = label;
            layer.repaint();
        }
    }
}
