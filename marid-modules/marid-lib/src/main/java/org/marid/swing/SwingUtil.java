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

import java.awt.*;
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
            } catch (RuntimeException x) {
                throw x;
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
}
