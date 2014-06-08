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
import org.marid.swing.SwingUtil;
import org.marid.swing.dnd.DndTarget;
import org.marid.swing.dnd.MaridTransferHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.function.Consumer;

import static java.awt.AWTEvent.*;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.event.MouseEvent.MOUSE_ENTERED;
import static java.awt.event.MouseEvent.MOUSE_EXITED;
import static org.marid.swing.geom.ShapeUtils.mouseEvent;
import static org.marid.swing.geom.ShapeUtils.ptAdd;

/**
 * @author Dmitry Ovchinnikov
 */
public class SchemaEditor extends JComponent implements DndTarget<Block> {

    protected final SchemaModel model;
    protected final AffineTransform transform = new AffineTransform();
    private Point mousePoint = new Point();
    private final Rectangle clip = new Rectangle();
    private AffineTransform mouseTransform = (AffineTransform) transform.clone();
    private Component curComponent;
    private Component movingComponent;
    private BlockLink currentLink;
    private Point movingComponentPoint;
    private Point movingComponentLocation;

    public SchemaEditor(SchemaModel model) {
        this.model = model;
        setFont(UIManager.getFont("Label.font"));
        setBackground(SystemColor.controlLtHighlight);
        setDoubleBuffered(true);
        setTransferHandler(new MaridTransferHandler());
        setForeground(SystemColor.controlDkShadow);
        enableEvents(MOUSE_EVENT_MASK | MOUSE_MOTION_EVENT_MASK | MOUSE_WHEEL_EVENT_MASK);
    }

    @Override
    protected void processMouseWheelEvent(MouseWheelEvent e) {
        final double s = 1.0 + e.getPreciseWheelRotation() / 10.0;
        final Point mp = SwingUtil.transform(transform::inverseTransform, e.getPoint());
        transform.translate(mp.getX(), mp.getY());
        transform.scale(s, s);
        transform.translate(-mp.getX(), -mp.getY());
        repaint();
        super.processMouseWheelEvent(e);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        onMouseEvent(e);
        super.processMouseEvent(e);
    }

    @Override
    protected void processMouseMotionEvent(MouseEvent e) {
        onMouseEvent(e);
        super.processMouseMotionEvent(e);
    }

    protected void onMouseEvent(MouseEvent e) {
        final Point mp = SwingUtil.transform(transform::inverseTransform, e.getPoint());
        switch (e.getID()) {
            case MouseEvent.MOUSE_DRAGGED:
                if (e.isShiftDown()) {
                    final Point p = SwingUtil.transform(mouseTransform::inverseTransform, e.getPoint());
                    transform.setTransform(mouseTransform);
                    transform.translate(p.getX() - mousePoint.getX(), p.getY() - mousePoint.getY());
                    repaint();
                    return;
                } else if (movingComponent != null && e.isControlDown()) {
                    movingComponent.setLocation(ptAdd(1, movingComponentLocation, 1, mp, -1, movingComponentPoint));
                    repaint();
                    currentLink = null;
                    return;
                }
                break;
            case MouseEvent.MOUSE_RELEASED:
                if (movingComponent != null) {
                    movingComponent = null;
                    movingComponentPoint = null;
                    movingComponentLocation = null;
                    return;
                }
            default:
                mousePoint = mp;
                mouseTransform = (AffineTransform) transform.clone();
                break;
        }
        for (int i = getComponentCount() - 1; i >= 0; i--) {
            final Component component = getComponent(i);
            final Point p = ptAdd(1, mp, -1, component.getLocation());
            if (component.contains(p)) {
                if (e.getID() == MouseEvent.MOUSE_PRESSED && e.isControlDown()) {
                    movingComponent = component;
                    movingComponentPoint = mp;
                    movingComponentLocation = component.getLocation();
                    break;
                }
                Component s = component;
                final Point sp = new Point(p);
                for (Component c = s.getComponentAt(sp); c != null && c != s; sp.translate(-s.getX(), -s.getY())) {
                    s = c;
                }
                try {
                    s.dispatchEvent(mouseEvent(s, e, e.getID(), sp));
                } catch (IllegalComponentStateException x) {
                    //ignore
                }
                if (s != curComponent) {
                    if (curComponent != null) {
                        curComponent.dispatchEvent(mouseEvent(curComponent, e, MOUSE_EXITED, sp));
                    }
                    s.dispatchEvent(mouseEvent(s, e, MOUSE_ENTERED, sp));
                    curComponent = s;
                }
                currentLink = null;
                repaint();
                return;
            }
        }
        if (curComponent != null) {
            curComponent.dispatchEvent(mouseEvent(curComponent, e, MOUSE_EXITED, mp));
            curComponent = null;
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
        g.setStroke(oldStroke);
        visitBlockComponents(bc -> {
            g.translate(bc.getX(), bc.getY());
            bc.print(g);
            g.translate(-bc.getX(), -bc.getY());
        });
    }

    @Override
    protected void paintChildren(Graphics g) {
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
}
