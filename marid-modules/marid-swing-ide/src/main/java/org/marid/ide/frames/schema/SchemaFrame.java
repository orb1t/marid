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

package org.marid.ide.frames.schema;

import images.Images;
import org.marid.bd.BlockComponent;
import org.marid.bd.schema.SchemaEditor;
import org.marid.bd.schema.SchemaModel;
import org.marid.bd.shapes.LinkShape;
import org.marid.bd.shapes.LinkShapeEvent;
import org.marid.dyn.MetaInfo;
import org.marid.ide.components.BlockMenuProvider;
import org.marid.ide.components.ProfileManager;
import org.marid.ide.frames.MaridFrame;
import org.marid.ide.profile.Profile;
import org.marid.spring.annotation.PrototypeComponent;
import org.marid.swing.SwingUtil;
import org.marid.swing.actions.MaridAction;
import org.marid.xml.XmlPersister;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.LayerUI;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.File;

import static java.awt.Color.RED;
import static java.awt.SystemColor.infoText;
import static java.lang.String.format;
import static javax.swing.BorderFactory.*;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
@MetaInfo(name = "Schema Editor")
public class SchemaFrame extends MaridFrame {

    protected final ProfileManager profileManager;
    protected final SchemaEditor schemaEditor;
    protected final JLayer<SchemaEditor> layer;
    protected final JMenu blocksMenu = new JMenu(s("Blocks"));

    protected File file;

    @MetaInfo(path = "/File/o/Open")
    public final Action openAction;

    @MetaInfo(path = "/File/s/Save")
    public final Action saveAction;

    @MetaInfo(path = "/File/s/SaveAs")
    public final Action saveAsAction;

    @MetaInfo(path = "/Schema/z/ZoomIn")
    public final Action zoomInAction;

    @MetaInfo(path = "/Schema/z/ZoomOut")
    public final Action zoomOutAction;

    @MetaInfo(path = "/Schema/z/ZoomReset")
    public final Action resetZoomAction;

    @MetaInfo(path = "/Schema/s/SelectionMode")
    public final Action selectionModeAction;

    @MetaInfo(path = "/Schema/r/Refresh")
    public final Action refreshAction;

    @MetaInfo(path = "/Schema/r/ResetInOut")
    public final Action resetIoAction;

    @MetaInfo(path = "/Schema/a/AlignToLeft")
    public final Action alignToLeftAction;

    @MetaInfo(path = "/Schema/a/AlignToRight")
    public final Action alignToRightAction;

    @MetaInfo(path = "b/Build/r/Build")
    public final Action buildAction;

    private final XmlPersister xmlPersister;
    private final ConfigurableApplicationContext applicationContext;

    @Autowired
    public SchemaFrame(BlockMenuProvider blockMenuProvider,
                       ProfileManager profileManager,
                       SchemaEditor schemaEditor,
                       XmlPersister xmlPersister,
                       ConfigurableApplicationContext applicationContext) {
        super("Schema");
        this.profileManager = profileManager;
        this.xmlPersister = xmlPersister;
        this.applicationContext = applicationContext;
        enableEvents(AWTEvent.COMPONENT_EVENT_MASK | AWTEvent.WINDOW_EVENT_MASK);
        centerPanel.add(layer = new JLayer<>(this.schemaEditor = schemaEditor, new SchemaLayerUI()));
        getContentPane().setBackground(getBackground());
        getJMenuBar().add(blocksMenu);
        blockMenuProvider.fillMenu(blocksMenu);

        openAction = new MaridAction("Open...", "open", this::open).setKey("control O").enableToolbar();
        saveAction = new MaridAction("Save", "save", this::save).setKey("control S").enableToolbar();
        saveAsAction = new MaridAction("Save As...", "save", this::saveAs).setKey("control shift S").enableToolbar();
        zoomInAction = new MaridAction("Zoom In", "zoomin", e -> schemaEditor.zoomIn()).setKey("control I").enableToolbar();
        zoomOutAction = new MaridAction("Zoom Out", "zoomout", e -> schemaEditor.zoomOut()).setKey("control B").enableToolbar();
        resetZoomAction = new MaridAction("Reset zoom", "zoom", e -> schemaEditor.resetZoom()).setKey("control Z").enableToolbar();
        selectionModeAction = new MaridAction("Selection mode", "selection", this::selectionMode).setKey("control J").setSelected(false).enableToolbar();
        refreshAction = new MaridAction("Refresh", "refresh", e -> schemaEditor.repaint()).setKey("F5").enableToolbar();
        resetIoAction = new MaridAction("Reset input/output selection", "reset", schemaEditor::resetInputOutputSelection).setKey("control shift T").enableToolbar();
        alignToLeftAction = new MaridAction("Align to left", "alignleft", schemaEditor::alignToLeft).setKey("control shift L").enableToolbar();
        alignToRightAction = new MaridAction("Align top right", "alignright", schemaEditor::alignToRight).setKey("control shift R").enableToolbar();
        buildAction = new MaridAction("Build", "hammer", e -> new SchemaModel(schemaEditor).getSchema().build()).setKey("F7").enableToolbar();
    }

    public void fireEvent(AWTEvent event) {
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

    public void selectionMode(Action action, ActionEvent event) {
        schemaEditor.setSelectionMode((boolean) action.getValue(Action.SELECTED_KEY));
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
                    final SchemaModel model = xmlPersister.load(SchemaModel.class, new StreamSource(chooser.getSelectedFile()));
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
        final SchemaModel model = applicationContext.getBean(SchemaModel.class, schemaEditor);
        xmlPersister.save(model, new StreamResult(file));
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

    protected class SchemaLayerUI extends LayerUI<SchemaEditor> {

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
