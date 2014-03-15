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

package org.marid.web;

import com.sun.net.httpserver.spi.HttpServerProvider;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov
 */
public class SimpleWebServerParameters extends AbstractWebServerParameters {

    int backlog = 0;
    HttpServerProvider httpServerProvider = HttpServerProvider.provider();
    InetSocketAddress address = new InetSocketAddress(8080);
    int webThreadPoolInitSize = 0;
    int webThreadPoolMaxSize = 16;
    long webThreadPoolKeepAliveTime = TimeUnit.MINUTES.toMillis(1L);
    Supplier<BlockingQueue<Runnable>> webBlockingQueueSupplier = SynchronousQueue::new;
    Function<SimpleWebServer, ThreadFactory> webPoolThreadFactory = s -> r -> new Thread(s.webPoolThreadGroup, r, r.toString(), getThreadStackSize());
    Function<SimpleWebServer, RejectedExecutionHandler> webRejectedExecutionHandler = s -> new ThreadPoolExecutor.CallerRunsPolicy();

    public SimpleWebServerParameters() {
        defaultPages = Arrays.asList("index.groovy", "index.html", "index.svg");
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public HttpServerProvider getHttpServerProvider() {
        return httpServerProvider;
    }

    public void setHttpServerProvider(HttpServerProvider httpServerProvider) {
        this.httpServerProvider = httpServerProvider;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public int getWebThreadPoolInitSize() {
        return webThreadPoolInitSize;
    }

    public void setWebThreadPoolInitSize(int webThreadPoolInitSize) {
        this.webThreadPoolInitSize = webThreadPoolInitSize;
    }

    public int getWebThreadPoolMaxSize() {
        return webThreadPoolMaxSize;
    }

    public void setWebThreadPoolMaxSize(int webThreadPoolMaxSize) {
        this.webThreadPoolMaxSize = webThreadPoolMaxSize;
    }

    public long getWebThreadPoolKeepAliveTime() {
        return webThreadPoolKeepAliveTime;
    }

    public void setWebThreadPoolKeepAliveTime(long webThreadPoolKeepAliveTime) {
        this.webThreadPoolKeepAliveTime = webThreadPoolKeepAliveTime;
    }

    public Supplier<BlockingQueue<Runnable>> getWebBlockingQueueSupplier() {
        return webBlockingQueueSupplier;
    }

    public void setWebBlockingQueueSupplier(Supplier<BlockingQueue<Runnable>> webBlockingQueueSupplier) {
        this.webBlockingQueueSupplier = webBlockingQueueSupplier;
    }

    public Function<SimpleWebServer, ThreadFactory> getWebPoolThreadFactory() {
        return webPoolThreadFactory;
    }

    public void setWebPoolThreadFactory(Function<SimpleWebServer, ThreadFactory> webPoolThreadFactory) {
        this.webPoolThreadFactory = webPoolThreadFactory;
    }

    public Function<SimpleWebServer, RejectedExecutionHandler> getWebRejectedExecutionHandler() {
        return webRejectedExecutionHandler;
    }

    public void setWebRejectedExecutionHandler(Function<SimpleWebServer, RejectedExecutionHandler> webRejectedExecutionHandler) {
        this.webRejectedExecutionHandler = webRejectedExecutionHandler;
    }
}
