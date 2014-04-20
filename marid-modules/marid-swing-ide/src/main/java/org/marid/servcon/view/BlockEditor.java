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

import org.marid.ide.servcon.ServconConfiguration;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.Uninterruptibles.sleepUninterruptibly;
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
public class BlockEditor extends JComponent implements DndTarget<Block>, Runnable {

    private final AffineTransform transform = new AffineTransform();
    protected final List<BlockLink> blockLinks = new CopyOnWriteArrayList<>();
    public final List<BlockView> blockViews = new ArrayList<>();
    private final Thread gaThread = new Thread(this);
    private final ForkJoinPool pool = new ForkJoinPool(Math.max(Runtime.getRuntime().availableProcessors(), 8));
    private Point mousePoint = new Point();
    private final Rectangle clip = new Rectangle();
    private AffineTransform mouseTransform = (AffineTransform) transform.clone();
    private Component currentComponent;
    private Component movingComponent;
    private Point movingComponentPoint;
    private Point movingComponentLocation;
    private volatile float mutationProbability = ServconConfiguration.mutationProbability.get();
    private volatile int incubatorSize = ServconConfiguration.incubatorSize.get();

    public BlockEditor() {
        setFont(requireNonNull(UIManager.getFont("Label.font")));
        setOpaque(true);
        setBackground(SystemColor.controlLtHighlight);
        setDoubleBuffered(true);
        setTransferHandler(new MaridTransferHandler());
        enableEvents(MOUSE_EVENT_MASK | MOUSE_MOTION_EVENT_MASK | MOUSE_WHEEL_EVENT_MASK);
        ServconConfiguration.mutationProbability.addConsumer(this, (o, n) -> mutationProbability = n);
        ServconConfiguration.incubatorSize.addConsumer(this, (o, n) -> incubatorSize = n);
    }

    public FontMetrics getFontMetrics() {
        return getFontMetrics(getFont());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        while (!pool.isShutdown()) {
            final List<ForkJoinTask<?>> tasks = new ArrayList<>(blockLinks.size());
            for (final BlockLink blockLink : blockLinks) {
                tasks.add(pool.submit(() -> {
                    final BlockLink.Incubator incubator = blockLink.createIncubator(incubatorSize);
                    final GaContext gaContext = new GaContext(blockLink) {
                        @Override
                        public final float getMutationProbability() {
                            return mutationProbability;
                        }
                    };
                    for (int i = 0; i < 100; i++) {
                        blockLink.doGA(gaContext, incubator);
                    }
                }));
            }
            tasks.forEach(ForkJoinTask::join);
            EventQueue.invokeLater(this::repaint);
            sleepUninterruptibly(100L, TimeUnit.MILLISECONDS);
        }
    }

    public void start() {
        gaThread.start();
    }

    public void stop() {
        pool.shutdown();
    }

    @SuppressWarnings("StringEquality")
    @Override
    protected void processEvent(AWTEvent e) {
        super.processEvent(e);
        MainSwitch:
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
                        final int x = p.x, y = p.y;
                        if (sub.getName() == BlockView.MOVEABLE && e.getID() == MOUSE_PRESSED) {
                            movingComponent = component;
                            movingComponentPoint = mp;
                            movingComponentLocation = component.getLocation();
                            break;
                        }
                        try {
                            sub.dispatchEvent(mouseEvent(sub, me, me.getID(), x, y));
                        } catch (IllegalComponentStateException ex) {
                            // ignore it
                        }
                        if (sub != currentComponent) {
                            if (currentComponent != null) {
                                currentComponent.dispatchEvent(mouseEvent(currentComponent, me, MOUSE_EXITED, x, y));
                            }
                            sub.dispatchEvent(mouseEvent(sub, me, MOUSE_ENTERED, x, y));
                            currentComponent = sub;
                        }
                        repaint(); // TODO: repaint within bounds
                        break MainSwitch;
                    }
                }
                if (currentComponent != null) {
                    currentComponent.dispatchEvent(mouseEvent(currentComponent, me, MOUSE_EXITED, mp.x, mp.y));
                }
                break;
        }
    }

    private MouseEvent mouseEvent(Component component, MouseEvent e, int id, int x, int y) {
        return new MouseEvent(component, id, e.getWhen(), e.getModifiers(), x, y,
                e.getXOnScreen(), e.getYOnScreen(), e.getClickCount(), e.isPopupTrigger(), e.getButton());
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;
        g.getClipBounds(clip);
        g.setBackground(getBackground());
        g.clearRect(clip.x, clip.y, clip.width, clip.height);
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g.transform(transform);
        for (final BlockLink blockLink : blockLinks) {
            blockLink.paint(g);
        }
        for (final BlockView block : blockViews) {
            g.translate(block.getX(), block.getY());
            block.print(g);
            g.translate(-block.getX(), -block.getY());
        }
    }

    @Override
    public Component add(Component comp) {
        final Component component = super.add(comp);
        if (comp instanceof BlockView) {
            blockViews.add((BlockView) comp);
        }
        return component;
    }

    @Override
    public void remove(Component comp) {
        super.remove(comp);
        if (comp instanceof BlockView) {
            blockViews.remove(comp);
        }
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
