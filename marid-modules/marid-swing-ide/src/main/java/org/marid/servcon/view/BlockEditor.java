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
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov.
 */
public class BlockEditor extends JComponent implements DndTarget<Block>, PrefSupport {

    private final AffineTransform transform = new AffineTransform();

    public BlockEditor() {
        setFont(requireNonNull(UIManager.getFont(getPref("font", "Label.font")), "Font is null"));
        setOpaque(true);
        setBackground(SystemColor.controlLtHighlight);
        setTransferHandler(new MaridTransferHandler());
        addMouseWheelListener(ev -> {
            final double s = 1.0 + ev.getPreciseWheelRotation() / 10.0;
            final Point mousePoint = SwingUtil.transform(transform::inverseTransform, ev.getPoint());
            transform.translate(mousePoint.getX(), mousePoint.getY());
            transform.scale(s, s);
            transform.translate(-mousePoint.getX(), -mousePoint.getY());
            repaint();
        });
        addMouseMotionListener(new EditorMouseMotionListener());
        registerKeyboardAction(a -> repaint(), KeyStroke.getKeyStroke("ESCAPE"), WHEN_FOCUSED);
    }

    public FontMetrics getFontMetrics() {
        return getFontMetrics(getFont());
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
                    block.printAll(g);
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

    private class EditorMouseMotionListener implements MouseMotionListener {

        private Point point = new Point();
        private AffineTransform t = (AffineTransform) transform.clone();

        @Override
        public void mouseDragged(MouseEvent e) {
            if (e.isShiftDown()) {
                final Point p = SwingUtil.transform(t::inverseTransform, e.getPoint());
                transform.setTransform(t);
                transform.translate(p.getX() - point.getX(), p.getY() - point.getY());
                repaint();
            }
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            point = SwingUtil.transform(transform::inverseTransform, e.getPoint());
            t = (AffineTransform) transform.clone();
        }
    }
}
