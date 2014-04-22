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
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

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
    protected final Deque<BlockLink> blockLinks = new ConcurrentLinkedDeque<>();
    public final Deque<BlockView> blockViews = new ConcurrentLinkedDeque<>();
    private final Thread gaThread = new Thread(this);
    private final ForkJoinPool pool = new ForkJoinPool(Math.max(Runtime.getRuntime().availableProcessors(), 8));
    private Point mousePoint = new Point();
    private final Rectangle clip = new Rectangle();
    private final Stroke lineStroke = new BasicStroke(2.0f);
    private AffineTransform mouseTransform = (AffineTransform) transform.clone();
    private Component curComponent;
    private BlockView movingComponent;
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
        setForeground(SystemColor.controlDkShadow);
        enableEvents(MOUSE_EVENT_MASK | MOUSE_MOTION_EVENT_MASK | MOUSE_WHEEL_EVENT_MASK);
        ServconConfiguration.mutationProbability.addConsumer(this, (o, n) -> mutationProbability = n);
        ServconConfiguration.incubatorSize.addConsumer(this, (o, n) -> incubatorSize = n);
        ServconConfiguration.linkType.addConsumer(this, (o, n) -> updateLinks());
        ServconConfiguration.species.addConsumer(this, (o, n) -> updateLinks());
    }

    private void updateLinks() {
        final BlockView.In[] ins = blockLinks.stream().map(l -> l.in).toArray(BlockView.In[]::new);
        final BlockView.Out[] outs = blockLinks.stream().map(l -> l.out).toArray(BlockView.Out[]::new);
        blockLinks.clear();
        final BlockLinkType linkType = ServconConfiguration.linkType.get();
        final int speciesCount = ServconConfiguration.species.get();
        for (int i = 0; i < ins.length; i++) {
            blockLinks.add(linkType.createBlockLink(speciesCount, ins[i], outs[i]));
        }
    }

    public FontMetrics getFontMetrics() {
        return getFontMetrics(getFont());
    }

    @Override
    public void run() {
        while (!pool.isShutdown()) {
            final List<ForkJoinTask<?>> tasks = new ArrayList<>(blockLinks.size());
            for (final BlockLink blockLink : blockLinks) {
                tasks.add(pool.submit(() -> {
                    blockLink.initIncubator(incubatorSize);
                    final GaContext gaContext = new GaContext(blockLink) {
                        @Override
                        public final float getMutationProbability() {
                            return mutationProbability;
                        }
                    };
                    for (int i = 0; i < 100; i++) {
                        blockLink.doGA(gaContext);
                    }
                    blockLink.getSpecie().fitness(gaContext);
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

    @Override
    protected void processEvent(AWTEvent e) {
        if (e.getID() == MOUSE_WHEEL) {
            final MouseWheelEvent me = (MouseWheelEvent) e;
            final double s = 1.0 + me.getPreciseWheelRotation() / 10.0;
            final Point mp = SwingUtil.transform(transform::inverseTransform, me.getPoint());
            transform.translate(mp.getX(), mp.getY());
            transform.scale(s, s);
            transform.translate(-mp.getX(), -mp.getY());
            repaint();
        } else {
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
                        return;
                    }
                    break;
                case MOUSE_RELEASED:
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
            for (final Iterator<BlockView> i = blockViews.descendingIterator(); i.hasNext(); ) {
                final BlockView component = i.next();
                int x = mp.x - component.getX(), y = mp.y - component.getY();
                if (component.contains(x, y)) {
                    Component sub = component;
                    while (true) {
                        final Component c = sub.getComponentAt(x, y);
                        if (c == null || c == sub) {
                            break;
                        }
                        sub = c;
                        x -= sub.getX(); y -= sub.getY();
                    }
                    if (BlockView.MOVEABLE.equals(sub.getName()) && e.getID() == MOUSE_PRESSED) {
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
                    if (sub != curComponent) {
                        if (curComponent != null) {
                            curComponent.dispatchEvent(mouseEvent(curComponent, me, MOUSE_EXITED, x, y));
                        }
                        sub.dispatchEvent(mouseEvent(sub, me, MOUSE_ENTERED, x, y));
                        curComponent = sub;
                    }
                    repaint();
                    return;
                }
            }
            if (curComponent != null) {
                curComponent.dispatchEvent(mouseEvent(curComponent, me, MOUSE_EXITED, mp.x, mp.y));
            }
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
        g.transform(transform);
        final Stroke oldStroke = g.getStroke();
        g.setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);
        g.setStroke(lineStroke);
        for (final BlockLink blockLink : blockLinks) {
            blockLink.paint(g);
        }
        g.setStroke(oldStroke);
        for (final BlockView block : blockViews) {
            g.translate(block.getX(), block.getY());
            block.print(g);
            g.translate(-block.getX(), -block.getY());
        }
    }

    public void add(BlockView blockView) {
        blockViews.add(blockView);
        add((Component) blockView);
    }

    public void remove(BlockView blockView) {
        for (final BlockLink blockLink : blockLinks) {
            if (blockLink.in.getBlockView() == blockView || blockLink.out.getBlockView() == blockView) {
                blockLinks.remove(blockLink);
            }
        }
        blockViews.remove(blockView);
        remove((Component) blockView);
        if (curComponent == blockView) {
            curComponent = null;
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
