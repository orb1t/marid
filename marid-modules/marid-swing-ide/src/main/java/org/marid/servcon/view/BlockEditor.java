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

package org.marid.servcon.view;

import org.marid.pref.PrefSupport;
import org.marid.servcon.model.Block;
import org.marid.swing.SwingUtil;
import org.marid.swing.dnd.DndTarget;
import org.marid.swing.dnd.MaridTransferHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;

import static java.awt.AWTEvent.*;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov.
 */
public class BlockEditor extends JComponent implements DndTarget<Block>, PrefSupport {

    private final AffineTransform transform = new AffineTransform();
    private Point mousePoint = new Point();
    private AffineTransform mouseTransform = (AffineTransform) transform.clone();

    public BlockEditor() {
        setFont(requireNonNull(UIManager.getFont(getPref("font", "Label.font")), "Font is null"));
        setOpaque(true);
        setBackground(SystemColor.controlLtHighlight);
        setTransferHandler(new MaridTransferHandler());
        enableEvents(MOUSE_EVENT_MASK | MOUSE_MOTION_EVENT_MASK | MOUSE_WHEEL_EVENT_MASK);
    }

    public FontMetrics getFontMetrics() {
        return getFontMetrics(getFont());
    }

    @Override
    protected void processEvent(AWTEvent e) {
        super.processEvent(e);
        switch (e.getID()) {
            case MouseEvent.MOUSE_WHEEL: {
                final MouseWheelEvent me = (MouseWheelEvent) e;
                final double s = 1.0 + me.getPreciseWheelRotation() / 10.0;
                final Point mp = SwingUtil.transform(transform::inverseTransform, me.getPoint());
                transform.translate(mp.getX(), mp.getY());
                transform.scale(s, s);
                transform.translate(-mp.getX(), -mp.getY());
                repaint();
                break;
            }
            case MouseEvent.MOUSE_CLICKED:
            case MouseEvent.MOUSE_DRAGGED:
            case MouseEvent.MOUSE_MOVED:
            case MouseEvent.MOUSE_PRESSED:
            case MouseEvent.MOUSE_RELEASED:
                final MouseEvent me = (MouseEvent) e;
                final Point mp = SwingUtil.transform(transform::inverseTransform, me.getPoint());
                switch (e.getID()) {
                    case MouseEvent.MOUSE_DRAGGED:
                        if (me.isShiftDown()) {
                            final Point p = SwingUtil.transform(mouseTransform::inverseTransform, me.getPoint());
                            transform.setTransform(mouseTransform);
                            transform.translate(p.getX() - mousePoint.getX(), p.getY() - mousePoint.getY());
                            repaint();
                        }
                        break;
                    default:
                        mousePoint = mp;
                        mouseTransform = (AffineTransform) transform.clone();
                        break;
                }
                for (final Component component : getComponents()) {
                    final Rectangle b = component.getBounds();
                    if (b.contains(mp)) {
                        Point p = new Point(mp.x - b.x, mp.y - b.y);
                        Component sub = component;
                        while (true) {
                            final Component c = sub.getComponentAt(p);
                            if (c == null || c == sub) {
                                break;
                            }
                            sub = c;
                            final Rectangle bounds = sub.getBounds();
                            p = new Point(p.x - bounds.x, p.y - bounds.y);
                        }
                        sub.dispatchEvent(new MouseEvent(sub, e.getID(), me.getWhen(), me.getModifiers(),
                                p.x, p.y, me.getXOnScreen(), me.getYOnScreen(), me.getClickCount(),
                                me.isPopupTrigger(), me.getButton()));
                        repaint();
                    }
                }
                break;
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;
        final Rectangle cb = g.getClipBounds();
        g.setBackground(getBackground());
        g.clearRect(cb.x, cb.y, cb.width, cb.height);
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        final AffineTransform oldTransform = g.getTransform();
        try {
            final AffineTransform t = new AffineTransform(oldTransform);
            t.concatenate(transform);
            g.setTransform(t);
            for (final Component component : getComponents()) {
                if (component instanceof SwingBlock) {
                    final SwingBlock block = (SwingBlock) component;
                    final Rectangle bb = block.getBounds();
                    g.translate(bb.x, bb.y);
                    block.print(g);
                    g.translate(-bb.x, -bb.y);
                }
            }
        } finally {
            g.setTransform(oldTransform);
        }
    }

    @Override
    public boolean dropDndObject(Block object, TransferHandler.TransferSupport support) {
        try {
            final Point dropPoint = new Point();
            transform.inverseTransform(support.getDropLocation().getDropPoint(), dropPoint);
            final SwingBlock block = new SwingBlock(this, object);
            block.setBounds(new Rectangle(dropPoint, block.getPreferredSize()));
            add(block);
            block.setVisible(false);
            repaint();
            return true;
        } catch (Exception x) {
            warning("Unable to transform coordinates", x);
            return false;
        }
    }
}
