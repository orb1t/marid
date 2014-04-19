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

import com.google.common.util.concurrent.Uninterruptibles;
import org.marid.pref.PrefSupport;
import org.marid.servcon.model.Block;
import org.marid.servcon.view.ga.GaContext;
import org.marid.swing.SwingUtil;
import org.marid.swing.dnd.DndTarget;
import org.marid.swing.dnd.MaridTransferHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.awt.AWTEvent.MOUSE_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_MOTION_EVENT_MASK;
import static java.awt.AWTEvent.MOUSE_WHEEL_EVENT_MASK;
import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.awt.event.MouseEvent.*;
import static java.util.Objects.requireNonNull;

/**
 * @author Dmitry Ovchinnikov.
 */
public class BlockEditor extends JComponent implements DndTarget<Block>, PrefSupport {

    private final AffineTransform transform = new AffineTransform();
    protected final List<BlockLink<?>> blockLinks = new CopyOnWriteArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final ForkJoinPool pool = new ForkJoinPool(8);
    private Point mousePoint = new Point();
    private AffineTransform mouseTransform = (AffineTransform) transform.clone();
    private Component currentComponent;
    private Component movingComponent;
    private Point movingComponentPoint;
    private Point movingComponentLocation;
    public final BlockLinkType blockLinkType;

    public BlockEditor() {
        blockLinkType = getPref("blockLinkType", BlockLinkType.LINE_LINK);
        setFont(requireNonNull(UIManager.getFont(getPref("font", "Label.font")), "Font is null"));
        setOpaque(true);
        setBackground(SystemColor.controlLtHighlight);
        setTransferHandler(new MaridTransferHandler());
        enableEvents(MOUSE_EVENT_MASK | MOUSE_MOTION_EVENT_MASK | MOUSE_WHEEL_EVENT_MASK);
    }

    public FontMetrics getFontMetrics() {
        return getFontMetrics(getFont());
    }

    public void start() {
        executorService.execute(() -> {
            while (!executorService.isShutdown()) {
                final List<ForkJoinTask<?>> tasks = new ArrayList<>(blockLinks.size());
                for (final BlockLink<?> blockLink : blockLinks) {
                    tasks.add(pool.submit(() -> {
                        for (int i = 0; i < 400; i++) {
                            blockLink.doGA(new GaContext(blockLink));
                        }
                    }));
                }
                tasks.forEach(ForkJoinTask::join);
                EventQueue.invokeLater(this::repaint);
                Uninterruptibles.sleepUninterruptibly(100L, TimeUnit.MILLISECONDS);
            }
        });
    }

    public void stop() {
        executorService.shutdown();
        pool.shutdown();
    }

    @SuppressWarnings("StringEquality")
    @Override
    protected void processEvent(AWTEvent e) {
        super.processEvent(e);
        MainSwitch: switch (e.getID()) {
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
                        } else if (movingComponent != null) {
                            final int locx = movingComponentLocation.x + mp.x - movingComponentPoint.x;
                            final int locy = movingComponentLocation.y + mp.y - movingComponentPoint.y;
                            movingComponent.setLocation(locx, locy);
                            repaint();
                            break MainSwitch;
                        }
                        break;
                    case MOUSE_RELEASED:
                        if (movingComponent != null) {
                            movingComponent = null;
                            movingComponentPoint = null;
                            movingComponentLocation = null;
                            break MainSwitch;
                        }
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
                        final int id = e.getID(), cc = me.getClickCount(), bt = me.getButton(), x = p.x, y = p.y;
                        final long t = me.getWhen();
                        final int m = me.getModifiers(), xs = me.getXOnScreen(), ys = me.getYOnScreen();
                        final boolean pt = me.isPopupTrigger();
                        final MouseEvent mev = new MouseEvent(sub, id, t, m, x, y, xs, ys, cc, pt, bt);
                        if (sub.getName() == BlockView.MOVEABLE && id == MOUSE_PRESSED) {
                            movingComponent = component;
                            movingComponentPoint = mp;
                            movingComponentLocation = component.getLocation();
                            break;
                        }
                        try {
                            sub.dispatchEvent(mev);
                        } catch (IllegalComponentStateException ex) {
                            // ignore it
                        }
                        if (sub != currentComponent) {
                            if (currentComponent != null) {
                                final MouseEvent ev = new MouseEvent(currentComponent, MOUSE_EXITED, t, m, x, y, xs, ys, cc, pt, bt);
                                currentComponent.dispatchEvent(ev);
                            }
                            sub.dispatchEvent(new MouseEvent(sub, MOUSE_ENTERED, t, m, x, y, xs, ys, cc, pt, bt));
                            currentComponent = sub;
                        }
                        repaint(); // TODO: repaint within bounds
                        break;
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
            for (final BlockLink blockLink : blockLinks) {
                blockLink.paint(g);
            }
            for (final Component component : getComponents()) {
                if (component instanceof BlockView) {
                    final BlockView block = (BlockView) component;
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
    public void remove(Component comp) {
        super.remove(comp);
        if (currentComponent == comp) {
            currentComponent = null;
        }
    }

    @Override
    public boolean dropDndObject(Block object, TransferHandler.TransferSupport support) {
        try {
            final Point dropPoint = new Point();
            transform.inverseTransform(support.getDropLocation().getDropPoint(), dropPoint);
            final BlockView block = new BlockView(this, object);
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
