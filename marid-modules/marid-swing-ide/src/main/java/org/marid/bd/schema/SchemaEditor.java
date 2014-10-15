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

import org.marid.bd.Block;
import org.marid.bd.BlockComponent;
import org.marid.bd.BlockLink;
import org.marid.bd.SingletonBlock;
import org.marid.bd.shapes.Link;
import org.marid.bd.shapes.LinkShape;
import org.marid.bd.shapes.LinkShapeEvent;
import org.marid.concurrent.MaridTimerTask;
import org.marid.swing.InputMaskType;
import org.marid.swing.SwingUtil;
import org.marid.swing.dnd.DndSource;
import org.marid.swing.dnd.DndTarget;
import org.marid.swing.dnd.MaridTransferHandler;
import org.marid.swing.geom.ShapeUtils;

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
import static java.awt.SystemColor.activeCaption;
import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.META_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.MouseEvent.*;
import static org.marid.concurrent.AtomicUtils.processDirty;
import static org.marid.swing.geom.ShapeUtils.mouseEvent;
import static org.marid.swing.geom.ShapeUtils.ptAdd;

/**
 * @author Dmitry Ovchinnikov
 */
public class SchemaEditor extends JComponent implements DndTarget<Block>, DndSource<Block>, SchemaFrameConfiguration {

    protected final SchemaFrame schemaFrame;
    protected final AffineTransform transform = new AffineTransform();
    private final Point mousePoint = new Point();
    private final Rectangle clip = new Rectangle();
    private AffineTransform mouseTransform = (AffineTransform) transform.clone();
    private Component curComponent;
    private final ComponentGroup selection = new ComponentGroup();
    private LinkShape currentLink;
    private volatile InputMaskType panType = PAN.get(), moveType = MOVE.get(), dragType = DRAG.get();
    private final AtomicBoolean dirty = new AtomicBoolean();
    private final Timer timer = new Timer(true);
    private Block selectedBlock;
    private final Set<LinkShape> links = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public SchemaEditor(SchemaFrame schemaFrame) {
        this.schemaFrame = schemaFrame;
        setFont(UIManager.getFont("Label.font"));
        setBackground(SystemColor.controlLtHighlight);
        setDoubleBuffered(true);
        setName(new UID().toString());
        setTransferHandler(new MaridTransferHandler());
        setForeground(SystemColor.controlDkShadow);
        enableEvents(MOUSE_EVENT_MASK | MOUSE_MOTION_EVENT_MASK | MOUSE_WHEEL_EVENT_MASK);
        PAN.addConsumer(this, n -> panType = n);
        MOVE.addConsumer(this, n -> moveType = n);
        DRAG.addConsumer(this, n -> dragType = n);
        LINK_SHAPE_TYPE.addConsumer(this, n -> EventQueue.invokeLater(() -> {
            final Map<BlockComponent.Output, BlockComponent.Input> map = new IdentityHashMap<>();
            links.forEach(shape -> map.put(shape.output, shape.input));
            links.clear();
            map.forEach(this::addLink);
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
        synchronized (getTreeLock()) {
            links.forEach(LinkShape::update);
        }
        invokeLater(super::repaint);
    }

    public LinkShape addLink(BlockComponent.Output output, BlockComponent.Input input) {
        final LinkShape link = LINK_SHAPE_TYPE.get().linkShapeFor(output, input);
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
            final BlockComponent.Output output = link.outputComponent.outputFor(link.output);
            final BlockComponent.Input input = link.inputComponent.inputFor(link.input);
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
                } else if (!selection.isEmpty() && moveType.isEnabled(e)) {
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
        final Point mp = SwingUtil.transform(transform::inverseTransform, e.getPoint());
        if (e.getID() == MouseEvent.MOUSE_RELEASED && !selection.isEmpty()) {
            selection.reset();
            return;
        }
        mousePoint.setLocation(mp);
        mouseTransform = (AffineTransform) transform.clone();
        if (dispatchBlocks(e)) {
            return;
        }
        if (dispatchLinks(e)) {
            return;
        }
        if (dispatchSelection(e)) {
            return;
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
                        break;
                    } else if (e.getID() == MOUSE_DRAGGED && dragType.isEnabled(e)) {
                        selectedBlock = ((BlockComponent) component).getBlock();
                        getTransferHandler().exportAsDrag(this, e, DND_COPY);
                        break;
                    }
                }
                final Component s = ShapeUtils.findChild(component, p);
                try {
                    s.dispatchEvent(mouseEvent(s, e, e.getID(), p));
                } catch (IllegalComponentStateException x) {
                    //ignore
                }
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
            case MOUSE_PRESSED:
                if ((e.getModifiers() & (ALT_DOWN_MASK | SHIFT_DOWN_MASK | META_DOWN_MASK)) == 0) {
                    selection.clear();
                    selection.startSelection(mousePoint);
                    repaint();
                    return true;
                }
                break;
            case MOUSE_RELEASED:
                if ((e.getModifiers() & (ALT_DOWN_MASK | SHIFT_DOWN_MASK | META_DOWN_MASK)) == 0) {
                    selection.endSelection(mousePoint, Arrays.asList(getComponents()));
                    repaint();
                    return true;
                }
                selection.reset();
                repaint();
                break;
        }
        return false;
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
        final Stroke oldStroke = g.getStroke();
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        for (final LinkShape linkShape : links) {
            g.setColor(currentLink == linkShape ? activeCaption : linkShape.getColor());
            g.setStroke(linkShape.getStroke());
            linkShape.paint(g);
        }
        g.setStroke(oldStroke);
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

    public void resetInputOutputSelection(ActionEvent actionEvent) {
        visitBlockComponents(bc -> {
            bc.getOutputs().forEach(o -> o.getButton().setSelected(false));
            bc.getInputs().forEach(i -> i.getButton().setSelected(false));
        });
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
        repaint();
    }

    @Override
    public boolean dropDndObject(Block object, TransferHandler.TransferSupport support) {
        try {
            final Point dropPoint = new Point();
            transform.inverseTransform(support.getDropLocation().getDropPoint(), dropPoint);
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
    public int getDndActions() {
        return DND_COPY;
    }

    @Override
    public Block getDndObject() {
        return selectedBlock instanceof SingletonBlock ? ((SingletonBlock) selectedBlock).blockPort() : selectedBlock;
    }

    @Override
    public void dndObjectExportDone(Block dndObject, int action) {
        selectedBlock = null;
    }
}
