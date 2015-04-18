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

import org.apache.commons.lang3.tuple.Pair;
import org.marid.bd.Block;
import org.marid.bd.BlockComponent;
import org.marid.bd.BlockLink;
import org.marid.bd.common.DndBlockSource;
import org.marid.bd.shapes.Link;
import org.marid.bd.shapes.LinkShape;
import org.marid.bd.shapes.LinkShapeEvent;
import org.marid.concurrent.MaridTimerTask;
import org.marid.ide.components.SchemaFrameConfiguration;
import org.marid.ide.frames.schema.SchemaFrame;
import org.marid.spring.annotation.PrototypeComponent;
import org.marid.swing.InputMaskType;
import org.marid.swing.SwingUtil;
import org.marid.swing.dnd.DndTarget;
import org.marid.swing.dnd.MaridTransferHandler;
import org.marid.swing.geom.ShapeUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.rmi.server.UID;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static java.awt.AWTEvent.MOUSE_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK;
import static java.awt.EventQueue.invokeLater;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.META_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.MouseEvent.*;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.tuple.Pair.of;
import static org.marid.bd.BlockComponent.Input;
import static org.marid.bd.BlockComponent.Output;
import static org.marid.concurrent.AtomicUtils.processDirty;
import static org.marid.swing.SwingUtil.componentStream;
import static org.marid.swing.geom.ShapeUtils.mouseEvent;
import static org.marid.swing.geom.ShapeUtils.ptAdd;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class SchemaEditor extends JComponent implements DndTarget<Block>, DndBlockSource {

    protected final AffineTransform transform = new AffineTransform();
    private final Point mousePoint = new Point();
    private final Rectangle clip = new Rectangle();
    private final AffineTransform mouseTransform = new AffineTransform();
    private Component curComponent;
    private final ComponentGroup selection = new ComponentGroup();
    private LinkShape currentLink;
    private volatile InputMaskType panType, dragType;
    private final AtomicBoolean dirty = new AtomicBoolean();
    private final Timer timer = new Timer(true);
    private Block selectedBlock;
    private final Set<LinkShape> links = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private boolean selectionMode;
    private final SchemaFrameConfiguration configuration;

    @Autowired
    public SchemaEditor(SchemaFrameConfiguration configuration) {
        this.configuration = configuration;
        panType = configuration.pan.get();
        dragType = configuration.drag.get();
        setFont(UIManager.getFont("Label.font"));
        setBackground(SystemColor.controlLtHighlight);
        setDoubleBuffered(true);
        setName(new UID().toString());
        setTransferHandler(new MaridTransferHandler());
        setForeground(SystemColor.controlDkShadow);
        enableEvents(MOUSE_EVENT_MASK | MOUSE_MOTION_EVENT_MASK | MOUSE_WHEEL_EVENT_MASK);
        configuration.pan.addListener(this, n -> panType = n);
        configuration.drag.addListener(this, n -> dragType = n);
        configuration.link.addListener(this, n -> EventQueue.invokeLater(() -> {
            final List<Pair<Output, Input>> pairs = links.stream().map(l -> of(l.output, l.input)).collect(toList());
            links.clear();
            pairs.forEach(p -> addLink(p.getLeft(), p.getRight()));
            repaint();
        }));
    }

    public void load(SchemaModel schemaModel) {
        selection.clear();
        removeAll();
        links.clear();
        final Map<Block, BlockComponent> blockMap = new IdentityHashMap<>();
        schemaModel.getBlocks().forEach(i -> {
            final BlockComponent blockComponent = i.getBlock().createComponent();
            blockComponent.setLocation(i.getLocation());
            blockComponent.updateBlock();
            add(blockComponent.getComponent());
            blockMap.put(blockComponent.getBlock(), blockComponent);
            blockComponent.setVisible(false);
        });
        schemaModel.getLinks().forEach(i -> {
            final BlockLink link = i.getBlockLink();
            final BlockComponent source = blockMap.get(link.getSource());
            final BlockComponent target = blockMap.get(link.getTarget());
            addLink(source.outputFor(link.getOutput()), target.inputFor(link.getInput()));
        });
        repaint();
    }

    public void start() {
        timer.schedule(new MaridTimerTask(() -> {
            if (!dirty.get()) {
                final Point mousePoint = MouseInfo.getPointerInfo().getLocation();
                final Point topLeft = new Point(0, 0);
                SwingUtilities.convertPointToScreen(topLeft, this);
                final Rectangle screenBounds = new Rectangle(topLeft, getSize());
                if (screenBounds.contains(mousePoint)) {
                    SwingUtilities.convertPointFromScreen(mousePoint, this);
                    final BlockComponent blockComponent = findBlockComponent(bc -> bc.getBounds().contains(mousePoint));
                    if (blockComponent != null) {
                        links.stream().filter(link -> link.isAssociatedWith(blockComponent)).forEach(link -> {
                            link.update();
                            dirty.compareAndSet(false, true);
                        });
                    }
                    final LinkShape clink = currentLink;
                    if (clink != null) {
                        clink.update();
                        dirty.compareAndSet(false, true);
                    }
                }
                if (dirty.getAndSet(false)) {
                    super.repaint();
                }
            } else {
                processDirty(dirty, this::update);
            }
        }), 50L, 50L);
    }

    public void stop() {
        timer.cancel();
    }

    public void update() {
        links.forEach(LinkShape::update);
        invokeLater(super::repaint);
    }

    public void setSelectionMode(boolean selectionMode) {
        if (this.selectionMode != selectionMode) {
            firePropertyChange("selectionMode", this.selectionMode, selectionMode);
        }
        this.selectionMode = selectionMode;
        if (!selectionMode) {
            selection.clear();
            repaint();
        }
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public LinkShape addLink(Output output, Input input) {
        final LinkShape link = configuration.link.get().linkShapeFor(output, input);
        links.add(link);
        info("Added link: {0}", link);
        return link;
    }

    public List<Link> removeAllLinks(BlockComponent component) {
        final List<Link> removed = new ArrayList<>();
        for (final Iterator<LinkShape> i = links.iterator(); i.hasNext(); ) {
            final LinkShape link = i.next();
            if (link.output.getBlockComponent() == component || link.input.getBlockComponent() == component) {
                i.remove();
                removed.add(new Link(link));
            }
        }
        return removed;
    }

    public Set<LinkShape> getLinkShapes() {
        return links;
    }

    public void removeLink(LinkShape link) {
        links.remove(link);
        repaint();
    }

    public void createLinks(List<Link> links) {
        for (final Link link : links) {
            final Output output = link.outputComponent.outputFor(link.output);
            final Input input = link.inputComponent.inputFor(link.input);
            if (output != null && input != null) {
                addLink(output, input);
            }
        }
        repaint();
    }

    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        zoom(1.0 + e.getPreciseWheelRotation() / 10.0, e.getPoint());
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        switch (e.getID()) {
            case MouseEvent.MOUSE_DRAGGED:
                if (panType.isEnabled(e)) {
                    final Point p = SwingUtil.transform(mouseTransform::inverseTransform, e.getPoint());
                    transform.setTransform(mouseTransform);
                    transform.translate(p.getX() - mousePoint.getX(), p.getY() - mousePoint.getY());
                    repaint();
                    return;
                } else if (isSelectionMode() && !selection.isEmpty() && !e.isControlDown()) {
                    final Point mp = SwingUtil.transform(transform::inverseTransform, e.getPoint());
                    selection.move(mp);
                    repaint();
                    currentLink = null;
                    return;
                } else if (!selection.isEmptySelection()) {
                    final Point mp = SwingUtil.transform(transform::inverseTransform, e.getPoint());
                    selection.updateSelection(mp);
                    repaint();
                }
                break;
        }
        processMouseEvent(e);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        mousePoint.setLocation(SwingUtil.transform(transform::inverseTransform, e.getPoint()));
        mouseTransform.setTransform(transform);
        if (isSelectionMode() && dispatchSelection(e)) {
            return;
        }
        if (!isSelectionMode() && dispatchBlocks(e)) {
            return;
        }
        if (!isSelectionMode() && dispatchLinks(e)) {
            return;
        }
        if (e.isPopupTrigger()) {
            new SchemaEditorPopupMenu().show(this, e.getX(), e.getY());
        }
    }

    private boolean dispatchBlocks(MouseEvent e) {
        for (int i = getComponentCount() - 1; i >= 0; i--) {
            final Component component = getComponent(i);
            final Point p = ptAdd(1, mousePoint, -1, component.getLocation());
            if (component.contains(p)) {
                if (component instanceof BlockComponent) {
                    if (e.isPopupTrigger()) {
                        final JPopupMenu popupMenu = ((BlockComponent) component).popupMenu();
                        popupMenu.show(this, e.getX(), e.getY());
                        return true;
                    } else if (e.getID() == MOUSE_DRAGGED && dragType.isEnabled(e)) {
                        selectedBlock = ((BlockComponent) component).getBlock();
                        getTransferHandler().exportAsDrag(this, e, DND_COPY);
                        return true;
                    }
                }
                final Component s = ShapeUtils.findChild(component, p);
                SwingUtil.dispatchEvent(mouseEvent(s, e, e.getID(), p));
                changeCurrentComponent(s, e, p);
                changeCurrentLink(null, e);
                return true;
            }
        }
        changeCurrentComponent(null, e, mousePoint);
        return false;
    }

    private boolean dispatchLinks(MouseEvent e) {
        for (final LinkShape linkShape : links) {
            final Shape shape = linkShape.getShape();
            if (ShapeUtils.contains(shape, mousePoint, 3.0)) {
                if (e.isPopupTrigger()) {
                    linkShape.popupMenu().show(this, e.getX(), e.getY());
                }
                changeCurrentLink(linkShape, e);
                return true;
            }
        }
        changeCurrentLink(null, e);
        return false;
    }

    private boolean dispatchSelection(MouseEvent e) {
        switch (e.getID()) {
            case MOUSE_CLICKED:
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
                    final BlockComponent component = findBlockComponent(c -> c.getBounds().contains(mousePoint));
                    if (component != null) {
                        if (!e.isShiftDown()) {
                            selection.clear();
                        }
                        selection.addComponent(component.getComponent());
                        repaint();
                        return true;
                    }
                }
                return false;
            case MOUSE_PRESSED:
            case MOUSE_RELEASED:
                if (e.isPopupTrigger() || e.getButton() != MouseEvent.BUTTON1) {
                    return false;
                } else if ((e.getModifiers() & (ALT_DOWN_MASK | SHIFT_DOWN_MASK | META_DOWN_MASK)) != 0) {
                    return false;
                } else if (selection.contains(mousePoint)) {
                    selection.reset();
                    return false;
                }
                switch (e.getID()) {
                    case MOUSE_PRESSED:
                        if (!e.isShiftDown() && !e.isControlDown()) {
                            selection.clear();
                        }
                        selection.startSelection(mousePoint);
                        break;
                    case MOUSE_RELEASED:
                        if (!selection.isEmptySelection()) {
                            selection.endSelection(mousePoint, Arrays.asList(getComponents()));
                        } else {
                            selection.reset();
                        }
                        break;
                }
                repaint();
                return true;
            default:
                return false;
        }
    }

    private void changeCurrentComponent(Component component, MouseEvent e, Point point) {
        if (component != curComponent) {
            if (component == null) {
                curComponent.dispatchEvent(mouseEvent(curComponent, e, MOUSE_EXITED, point));
            } else {
                if (curComponent == null) {
                    component.dispatchEvent(mouseEvent(component, e, MOUSE_ENTERED, point));
                } else {
                    curComponent.dispatchEvent(mouseEvent(curComponent, e, MOUSE_EXITED, point));
                    component.dispatchEvent(mouseEvent(component, e, MOUSE_ENTERED, point));
                }
            }
            curComponent = component;
            repaint();
        }
    }

    private void changeCurrentLink(LinkShape newLink, MouseEvent e) {
        if (newLink != currentLink) {
            if (newLink == null) {
                fireLinkEvent(currentLink, LinkShapeEvent.MOUSE_EXITED, e);
            } else {
                if (currentLink == null) {
                    fireLinkEvent(newLink, LinkShapeEvent.MOUSE_ENTERED, e);
                } else {
                    fireLinkEvent(currentLink, LinkShapeEvent.MOUSE_EXITED, e);
                    fireLinkEvent(newLink, LinkShapeEvent.MOUSE_ENTERED, e);
                }
            }
            currentLink = newLink;
            repaint();
        }
    }

    private void fireLinkEvent(LinkShape link, int id, MouseEvent e) {
        final SchemaFrame schemaFrame = (SchemaFrame) SwingUtilities.windowForComponent(this);
        schemaFrame.fireEvent(new LinkShapeEvent(link, id, e));
        final AbstractButton outputButton = link.output.getButton();
        final AbstractButton inputButton = link.input.getButton();
        switch (id) {
            case LinkShapeEvent.MOUSE_ENTERED:
                outputButton.dispatchEvent(mouseEvent(outputButton, e, MOUSE_ENTERED, new Point()));
                inputButton.dispatchEvent(mouseEvent(inputButton, e, MOUSE_ENTERED, new Point()));
                break;
            case LinkShapeEvent.MOUSE_EXITED:
                outputButton.dispatchEvent(mouseEvent(outputButton, e, MOUSE_EXITED, new Point()));
                inputButton.dispatchEvent(mouseEvent(inputButton, e, MOUSE_EXITED, new Point()));
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;
        g.getClipBounds(clip);
        g.setBackground(getBackground());
        g.clearRect(clip.x, clip.y, clip.width, clip.height);
        g.transform(transform);
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        links.forEach(l -> l.paint(g, l == currentLink));
        for (int i = getComponentCount() - 1; i >= 0; i--) {
            final Component c = getComponent(i);
            g.translate(c.getX(), c.getY());
            c.print(g);
            g.translate(-c.getX(), -c.getY());
        }
        selection.paint(g);
    }

    @Override
    protected void paintChildren(Graphics g) {
    }

    @Override
    public void repaint() {
        dirty.set(true);
    }

    public void alignToLeft(ActionEvent actionEvent) {
        selection.toLeft();
        repaint();
    }

    public void alignToRight(ActionEvent actionEvent) {
        selection.toRight();
        repaint();
    }

    public void resetInputOutputSelection(ActionEvent actionEvent) {
        visitBlockComponents(bc -> {
            bc.getOutputs().forEach(o -> o.getButton().setSelected(false));
            bc.getInputs().forEach(i -> i.getButton().setSelected(false));
        });
        repaint();
    }

    public void visitBlockComponents(Consumer<BlockComponent> componentConsumer) {
        synchronized (getTreeLock()) {
            for (int i = getComponentCount() - 1; i >= 0; i--) {
                final Component c = getComponent(i);
                if (c instanceof BlockComponent) {
                    componentConsumer.accept((BlockComponent) c);
                }
            }
        }
    }

    public BlockComponent findBlockComponent(Predicate<BlockComponent> predicate) {
        synchronized (getTreeLock()) {
            for (int i = getComponentCount() - 1; i >= 0; i--) {
                final Component c = getComponent(i);
                if (c instanceof BlockComponent) {
                    final BlockComponent blockComponent = (BlockComponent) c;
                    if (predicate.test(blockComponent)) {
                        return blockComponent;
                    }
                }
            }
        }
        return null;
    }

    public void zoom(double scale, Point point) {
        final Point mp = SwingUtil.transform(transform::inverseTransform, point);
        transform.translate(mp.getX(), mp.getY());
        transform.scale(scale, scale);
        transform.translate(-mp.getX(), -mp.getY());
        repaint();
    }

    public void zoomIn() {
        zoom(1.1, new Point(getWidth() / 2, getHeight() / 2));
    }

    public void zoomOut() {
        zoom(0.9, new Point(getWidth() / 2, getHeight() / 2));
    }

    public void resetZoom() {
        transform.setToIdentity();
        final int minX = componentStream(this).mapToInt(Component::getX).min().orElse(0);
        final int minY = componentStream(this).mapToInt(Component::getY).min().orElse(0);
        componentStream(this).forEach(c -> c.setLocation(c.getX() - minX + 10, c.getY() - minY + 10));
        repaint();
    }

    @Override
    public boolean dropDndObject(Block object, TransferHandler.TransferSupport support) {
        return dropBlock(object, support.getDropLocation().getDropPoint(), support.getDropAction());
    }

    public boolean dropBlock(Block object, Point dropPoint, int action) {
        try {
            transform.inverseTransform(new Point(dropPoint), dropPoint);
            final BlockComponent blockComponent = object.createComponent();
            blockComponent.setBounds(new Rectangle(dropPoint, blockComponent.getPreferredSize()));
            add(blockComponent.getComponent());
            blockComponent.setVisible(false);
            repaint();
            return true;
        } catch (Exception x) {
            warning("Unable to transform coordinates", x);
            return false;
        }
    }

    @Override
    public Block getDndObject() {
        return selectedBlock;
    }

    @Override
    public void dndObjectExportDone(Block dndObject, int action) {
        selectedBlock = null;
    }
}
