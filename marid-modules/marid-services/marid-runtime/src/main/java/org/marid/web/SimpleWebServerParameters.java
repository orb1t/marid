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

    public int backlog = 0;
    public HttpServerProvider httpServerProvider = HttpServerProvider.provider();
    public InetSocketAddress address = new InetSocketAddress(8080);
    public int webThreadPoolInitSize = 0;
    public int webThreadPoolMaxSize = 16;
    public long webThreadPoolKeepAliveTime = TimeUnit.MINUTES.toMillis(1L);
    public Supplier<BlockingQueue<Runnable>> webBlockingQueueSupplier = SynchronousQueue::new;
    public Function<SimpleWebServer, ThreadFactory> webPoolThreadFactory = s -> r -> new Thread(s.webPoolThreadGroup, r, r.toString(), threadStackSize);
    public Function<SimpleWebServer, RejectedExecutionHandler> webRejectedExecutionHandler = s -> new ThreadPoolExecutor.CallerRunsPolicy();

    public SimpleWebServerParameters() {
        defaultPages = Arrays.asList("index.groovy", "index.html", "index.svg");
    }
}
