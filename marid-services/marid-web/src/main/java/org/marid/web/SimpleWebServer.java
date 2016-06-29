/*
 * Copyright (c) 2015 Dmitry Ovchinnikov
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

package org.marid.web;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;
import org.marid.logging.LogSupport;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Runtime.getRuntime;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.marid.concurrent.ThreadPools.CALLER_RUNS_POLICY;

/**
 * @author Dmitry Ovchinnikov.
 */
@Resource(name = "simpleWebServer", description = "Simple web server", type = SimpleWebServer.class)
public class SimpleWebServer implements Closeable, LogSupport {

    private final HttpServer httpServer;
    private final int shutdownTimeoutSeconds;
    private final InetSocketAddress address;

    public SimpleWebServer(SimpleWebServerProperties properties) {
        shutdownTimeoutSeconds = properties.getShutdownTimeoutSeconds();
        address = new InetSocketAddress(properties.getHost(), properties.getPort());
        try {
            if (properties.getSslContext() != null) {
                final HttpsServer server = HttpsServer.create(address, properties.getBacklog());
                server.setHttpsConfigurator(new HttpsConfigurator(properties.getSslContext()));
                httpServer = server;
            } else {
                httpServer = HttpServer.create(address, properties.getBacklog());
            }
            httpServer.setExecutor(properties.getExecutorService());
            properties.getHandlerMap().forEach((path, handler) -> {
                final HttpContext context = httpServer.createContext(path, handler);
                handler.configure(context);
            });
        } catch (IOException x) {
            throw new UncheckedIOException(x);
        }
    }

    public ExecutorService getExecutorService() {
        return (ExecutorService) httpServer.getExecutor();
    }

    public void setExecutorService(ExecutorService executorService) {
        httpServer.setExecutor(executorService);
    }

    @PostConstruct
    public void start() {
        if (httpServer.getExecutor() == null) {
            final int tc = Math.max(4, getRuntime().availableProcessors());
            final BlockingQueue<Runnable> queue = new ArrayBlockingQueue<>(1024);
            final AtomicInteger threadCounter = new AtomicInteger();
            final ThreadFactory tf = r -> {
                final ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
                final String name = SimpleWebServer.this + "-" + threadCounter.getAndIncrement();
                return new Thread(threadGroup, r, name, 128L * 1024L);
            };
            httpServer.setExecutor(new ThreadPoolExecutor(tc, tc, 10L, SECONDS, queue, tf, CALLER_RUNS_POLICY));
        }
        httpServer.start();
        log(INFO, "Started on port {0}", getPort());
    }

    @Override
    public void close() throws IOException {
        try {
            httpServer.stop(shutdownTimeoutSeconds);
            log(INFO, "Stopped");
        } catch (Exception x) {
            log(WARNING, "Unable to stop", x);
        }
        final ExecutorService executorService = Objects.requireNonNull(getExecutorService());
        try {
            executorService.shutdown();
            if (executorService.awaitTermination(10L, SECONDS)) {
                log(INFO, "Executor service is stopped");
                return;
            }
        } catch (InterruptedException x) {
            log(WARNING, "Interrupted", x);
        }
        executorService.shutdownNow();
        log(INFO, "Executor service is stopped");
    }

    public int getPort() {
        try {
            return httpServer.getAddress().getPort();
        } catch (Exception x) {
            return address.getPort();
        }
    }

    @Override
    public String toString() {
        try {
            return String.format("%s(%s)", getClass().getSimpleName(), httpServer.getAddress());
        } catch (Exception x) {
            return String.format("%s(%s)", getClass().getSimpleName(), address);
        }
    }
}
