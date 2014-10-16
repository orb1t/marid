/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.swing;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * @author Dmitry Ovchinnikov
 */
public class SwingUtil {

    public static void execute(Runnable runnable) throws IllegalThreadStateException, IllegalStateException {
        if (EventQueue.isDispatchThread()) {
            runnable.run();
        } else {
            final FutureTask<Void> task = new FutureTask<>(runnable, null);
            EventQueue.invokeLater(task);
            try {
                task.get();
            } catch (InterruptedException x) {
                throw new IllegalThreadStateException(x.getMessage());
            } catch (ExecutionException x) {
                if (x.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) x.getCause();
                } else {
                    throw new IllegalStateException(x.getCause());
                }
            }
        }
    }

    public static <T> T call(Callable<T> callable) throws IllegalThreadStateException, IllegalStateException {
        if (EventQueue.isDispatchThread()) {
            try {
                return callable.call();
            } catch (Exception x) {
                throw new IllegalStateException(x);
            }
        } else {
            final FutureTask<T> task = new FutureTask<>(callable);
            EventQueue.invokeLater(task);
            try {
                return task.get();
            } catch (InterruptedException x) {
                throw new IllegalThreadStateException(x.getMessage());
            } catch (ExecutionException x) {
                if (x.getCause() instanceof RuntimeException) {
                    throw (RuntimeException) x.getCause();
                } else {
                    throw new IllegalStateException(x.getCause());
                }
            }
        }
    }

    public static void addDisposeListener(JComponent component, Runnable disposeListener) {
        component.addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                System.out.println("A:" + event.getAncestor());
                if (event.getAncestor() instanceof Window) {
                    ((Window) event.getAncestor()).addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosed(WindowEvent e) {
                            disposeListener.run();
                        }
                    });
                    component.removeAncestorListener(this);
                } else if (event.getAncestor() instanceof JInternalFrame) {
                    ((JInternalFrame) event.getAncestor()).addInternalFrameListener(new InternalFrameAdapter() {
                        @Override
                        public void internalFrameClosed(InternalFrameEvent e) {
                            disposeListener.run();
                        }
                    });
                    component.removeAncestorListener(this);
                }
            }

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                System.out.println("R:" + event.getAncestor());
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }
        });
    }

    public static Point transform(CoordinateTransformFunction transform, Point point) {
        final Point target = new Point();
        try {
            transform.transform(point, target);
            return target;
        } catch (Exception x) {
            throw new IllegalStateException(x);
        }
    }

    public static Rectangle transform(CoordinateTransformFunction transform, Rectangle rectangle) {
        final Point p1 = rectangle.getLocation();
        final Point p2 = new Point(p1.x + rectangle.width, p1.y + rectangle.height);
        final Point tp1 = transform(transform, p1);
        final Point tp2 = transform(transform, p2);
        return new Rectangle(tp1.x, tp1.y, tp2.x - tp1.x, tp2.y - tp1.y);
    }

    public static Color color(Color base, int alpha) {
        return new Color(base.getRed(), base.getGreen(), base.getBlue(), alpha);
    }

    public static void dispatchEvent(Component component, AWTEvent event) {
        try {
            component.dispatchEvent(event);
        } catch (IllegalComponentStateException x) {
            // ignore
        }
    }

    public static void dispatchEvent(AWTEvent event) {
        dispatchEvent((Component) event.getSource(), event);
    }

    @FunctionalInterface
    public static interface CoordinateTransformFunction {

        Point2D transform(Point2D source, Point2D target) throws Exception;
    }
}
