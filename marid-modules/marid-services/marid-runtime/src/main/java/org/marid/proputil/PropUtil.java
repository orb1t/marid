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

package org.marid.proputil;

import com.google.common.base.Supplier;
import groovy.lang.Closure;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;

import static org.marid.groovy.GroovyRuntime.get;

/**
 * @author Dmitry Ovchinnikov
 */
public class PropUtil {

    @SuppressWarnings("unchecked")
    public static BlockingQueue<Runnable> getBlockingQueue(Map params, String key, int def) {
        final Object value = params.get(key);
        if (value == null) {
            return new SynchronousQueue<>();
        } else if (value instanceof BlockingQueue) {
            return (BlockingQueue<Runnable>) value;
        } else if (value instanceof String) {
            switch ((String) value) {
                case "linked":
                    return new LinkedBlockingQueue<>();
                case "array":
                    return new ArrayBlockingQueue<>(def == 0 ? Runtime.getRuntime().availableProcessors() * 2 : def);
                case "synchronous":
                    return new SynchronousQueue<>();
                default:
                    throw new IllegalArgumentException("Invalid blocking queue type: " + value);
            }
        } else if (value instanceof Supplier) {
            return ((Supplier<BlockingQueue<Runnable>>) value).get();
        } else if (value instanceof Number) {
            return new ArrayBlockingQueue<>(((Number) value).intValue());
        } else if (value instanceof Boolean) {
            return new SynchronousQueue<>((Boolean) value);
        } else if (value instanceof Closure) {
            return ((Closure<BlockingQueue<Runnable>>) value).call(params);
        } else {
            return get(BlockingQueue.class, params, key, new SynchronousQueue());
        }
    }

    public static RejectedExecutionHandler getRejectedExecutionHandler(Map params, String key) {
        final Object value = params.get(key);
        if (value == null) {
            return new CallerRunsPolicy();
        } else if (value instanceof RejectedExecutionHandler) {
            return (RejectedExecutionHandler) value;
        } else if (value instanceof String) {
            switch ((String) value) {
                case "callerRunsPolicy":
                    return new CallerRunsPolicy();
                case "discardPolicy":
                    return new DiscardPolicy();
                case "discardOldestPolicy":
                    return new DiscardOldestPolicy();
                case "abortPolicy":
                    return new AbortPolicy();
                default:
                    throw new IllegalArgumentException("Invalid rejection handler: " + value);
            }
        } else if (value instanceof Supplier) {
            return (RejectedExecutionHandler) ((Supplier) value).get();
        } else if (value instanceof Closure) {
            return (RejectedExecutionHandler) ((Closure) value).call(params);
        } else {
            return get(RejectedExecutionHandler.class, params, key, new CallerRunsPolicy());
        }
    }

    public static ThreadFactory getThreadFactory(Map params, String key, final ThreadGroup threadGroup, final boolean daemon, final int stackSize) {
        final Object value = params.get(key);
        if (value == null) {
            return new ThreadFactory() {

                private short n;

                @SuppressWarnings("NullableProblems")
                @Override
                public Thread newThread(Runnable r) {
                    final Thread thread = new Thread(threadGroup, r, Short.toString(n++), stackSize);
                    thread.setDaemon(daemon);
                    return thread;
                }
            };
        } else if (value instanceof String) {
            final String format = (String) value;
            return new ThreadFactory() {

                private short n;

                @SuppressWarnings("NullableProblems")
                @Override
                public Thread newThread(Runnable r) {
                    final Thread thread = new Thread(threadGroup, r, String.format(format, n++, r), stackSize);
                    thread.setDaemon(daemon);
                    return thread;
                }
            };
        } else if (value instanceof ThreadFactory) {
            return (ThreadFactory) value;
        } else if (value instanceof Supplier) {
            return (ThreadFactory) ((Supplier) value).get();
        } else if (value instanceof Closure) {
            return (ThreadFactory) ((Closure) value).call(params);
        } else {
            throw new IllegalArgumentException("Illegal value for thread factory: " + value);
        }
    }

    public static InetSocketAddress getInetSocketAddress(Map params, String key, int def) {
        final Object value = params.get(key);
        if (value == null) {
            return new InetSocketAddress(def);
        } else if (value instanceof Number) {
            return new InetSocketAddress(((Number) value).intValue());
        } else if (value instanceof byte[]) {
            try {
                return new InetSocketAddress(InetAddress.getByAddress((byte[]) value), def);
            } catch (UnknownHostException x) {
                throw new IllegalArgumentException("Invalid IP address", x);
            }
        } else if (value instanceof InetAddress) {
            return new InetSocketAddress((InetAddress) value, def);
        } else if (value instanceof String) {
            return new InetSocketAddress((String) value, def);
        } else if (value instanceof InetSocketAddress) {
            return (InetSocketAddress) value;
        } else if (value instanceof Supplier) {
            return (InetSocketAddress) ((Supplier) value).get();
        } else if (value instanceof Closure) {
            return (InetSocketAddress) ((Closure) value).call(params);
        } else {
            return get(InetSocketAddress.class, params, key, new InetSocketAddress(def));
        }
    }
}
