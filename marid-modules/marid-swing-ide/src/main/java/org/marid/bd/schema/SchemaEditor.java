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
import org.marid.bd.shapes.LinkShape;
import org.marid.concurrent.MaridTimerTask;
import org.marid.functions.ReturnObjectException;
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
import java.util.*;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static java.awt.AWTEvent.MOUSE_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK;
import static java.awt.EventQueue.invokeLater;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.event.MouseEvent.*;
import static org.marid.concurrent.AtomicUtils.processDirty;
import static org.marid.swing.geom.ShapeUtils.mouseEvent;
import static org.marid.swing.geom.ShapeUtils.ptAdd;

/**
 * @author Dmitry Ovchinnikov
 */
public class SchemaEditor extends JComponent implements DndTarget<Block>, DndSource<Block>, SchemaFrameConfiguration {

    private static final Stroke STROKE = new BasicStroke(1.0f);
    private static final Stroke SELECTED_STROKE = new BasicStroke(3.0f);

    protected final AffineTransform transform = new AffineTransform();
    private Point mousePoint = new Point();
    private final Rectangle clip = new Rectangle();
    private AffineTransform mouseTransform = (AffineTransform) transform.clone();
    private Component curComponent;
    private Component movingComponent;
    private LinkShape currentLink;
    private Point movingComponentPoint;
    private Point movingComponentLocation;
    private volatile InputMaskType panType = PAN.get();
    private volatile InputMaskType moveType = MOVE.get();
    private volatile InputMaskType dragType = DRAG.get();
    private final AtomicBoolean dirty = new AtomicBoolean();
    private final Timer timer = new Timer(true);
    private Block draggingBlock;
    private final Set<LinkShape> links = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public SchemaEditor() {
        setFont(UIManager.getFont("Label.font"));
        setBackground(SystemColor.controlLtHighlight);
        setDoubleBuffered(true);
        setOpaque(false);
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
        }));
    }

    public void start() {
        timer.schedule(new MaridTimerTask(() -> {
            if (!dirty.get()) {
                final Point mousePoint = MouseInfo.getPointerInfo().getLocation();
                final Point topLeft = new Point(0, 0);
                SwingUtilities.convertPointToScreen(topLeft, this);
                final Rectangle screenBounds = new Rectangle(topLeft, getSize());
                boolean updated = false;
                if (screenBounds.contains(mousePoint)) {
                    SwingUtilities.convertPointFromScreen(mousePoint, this);
                    try {
                        visitBlockComponents(bc -> {
                            if (bc.getBounds().contains(mousePoint)) {
                                throw new ReturnObjectException(bc);
                            }
                        });
                    } catch (ReturnObjectException x) {
                        final BlockComponent blockComponent = x.getResult();
                        final Set<BlockComponent.Output> outputs = new HashSet<>(blockComponent.getOutputs());
                        final Set<BlockComponent.Input> inputs = new HashSet<>(blockComponent.getInputs());
                        for (final LinkShape link : links) {
                            if (outputs.contains(link.output) || inputs.contains(link.input)) {
                                link.update();
                                updated = true;
                            }
                        }
                    }
                    final LinkShape clink = currentLink;
                    if (clink != null) {
                        clink.update();
                        updated = true;
                    }
                    if (updated) {
                        super.repaint();
                    }
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

    public void addLink(BlockComponent.Output output, BlockComponent.Input input) {
        links.add(LINK_SHAPE_TYPE.get().linkShapeFor(output, input));
    }

    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        zoom(1.0 + e.getPreciseWheelRotation() / 10.0, e.getPoint());
        super.processMouseWheelEvent(e);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        onMouseEvent(e);
        super.processMouseEvent(e);
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
                } else if (movingComponent != null && moveType.isEnabled(e)) {
                    final Point mp = SwingUtil.transform(transform::inverseTransform, e.getPoint());
                    movingComponent.setLocation(ptAdd(1, movingComponentLocation, 1, mp, -1, movingComponentPoint));
                    repaint();
                    currentLink = null;
                    return;
                }
                break;
        }
        onMouseEvent(e);
        super.processMouseMotionEvent(e);
    }

    protected void onMouseEvent(MouseEvent e) {
        final Point mp = SwingUtil.transform(transform::inverseTransform, e.getPoint());
        if (e.getID() == MouseEvent.MOUSE_RELEASED && movingComponent != null) {
            movingComponent = null;
            return;
        }
        mousePoint = mp;
        mouseTransform = (AffineTransform) transform.clone();
        for (int i = getComponentCount() - 1; i >= 0; i--) {
            final Component component = getComponent(i);
            final Point p = ptAdd(1, mp, -1, component.getLocation());
            if (component.contains(p)) {
                if (component instanceof BlockComponent) {
                    if (e.isPopupTrigger()) {
                        final JPopupMenu popupMenu = ((BlockComponent) component).popupMenu();
                        popupMenu.show(this, e.getX(), e.getY());
                        break;
                    } else if (e.getID() == MouseEvent.MOUSE_PRESSED && moveType.isEnabled(e)) {
                        prepareMove(component, mp);
                        break;
                    } else if (e.getID() == MOUSE_DRAGGED && dragType.isEnabled(e)) {
                        draggingBlock = ((BlockComponent) component).getBlock();
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
                if (s != curComponent) {
                    if (curComponent != null) {
                        curComponent.dispatchEvent(mouseEvent(curComponent, e, MOUSE_EXITED, p));
                    }
                    s.dispatchEvent(mouseEvent(s, e, MOUSE_ENTERED, p));
                    curComponent = s;
                }
                currentLink = null;
                repaint();
                return;
            }
        }
        if (curComponent != null) {
            curComponent.dispatchEvent(mouseEvent(curComponent, e, MOUSE_EXITED, mp));
            repaint();
            curComponent = null;
        }
        for (final LinkShape linkShape : links) {
            final Shape shape = linkShape.getShape();
            if (ShapeUtils.contains(shape, mp, 3.0)) {
                if (currentLink != linkShape) {
                    currentLink = linkShape;
                    repaint();
                }
                return;
            }
        }
        if (currentLink != null) {
            currentLink = null;
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;
        g.getClipBounds(clip);
        g.transform(transform);
        final Stroke oldStroke = g.getStroke();
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        for (final LinkShape linkShape : links) {
            if (currentLink == linkShape) {
                g.setColor(SystemColor.activeCaption);
                g.setStroke(SELECTED_STROKE);
            } else {
                g.setColor(getForeground());
                g.setStroke(STROKE);
            }
            linkShape.paint(g);
        }
        g.setStroke(oldStroke);
        for (int i = getComponentCount() - 1; i >= 0; i--) {
            final Component c = getComponent(i);
            g.translate(c.getX(), c.getY());
            c.print(g);
            g.translate(-c.getX(), -c.getY());
        }
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

    protected void prepareMove(Component component, Point point) {
        movingComponent = component;
        movingComponentPoint = point;
        movingComponentLocation = component.getLocation();
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
        return draggingBlock;
    }
}
