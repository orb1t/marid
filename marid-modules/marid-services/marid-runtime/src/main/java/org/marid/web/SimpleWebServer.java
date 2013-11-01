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

package org.marid.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.spi.HttpServerProvider;
import groovy.lang.Closure;
import groovy.lang.GroovyCodeSource;
import org.marid.groovy.GroovyRuntime;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;

import static com.google.common.io.Files.getFileExtension;
import static com.google.common.io.Files.getNameWithoutExtension;
import static java.net.HttpURLConnection.*;
import static org.marid.methods.LogMethods.info;
import static org.marid.methods.LogMethods.warning;
import static org.marid.methods.PropMethods.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class SimpleWebServer extends AbstractWebServer implements HttpHandler {

    protected final int stopTimeout;
    protected final HttpServer server;
    protected final Path webDir;
    protected final Map<Path, HttpHandler> handlerMap = new ConcurrentHashMap<>();

    public SimpleWebServer(Map params) throws IOException {
        super(params);
        stopTimeout = get(params, int.class, "stopTimeout", 60);
        final HttpServerProvider dsp = HttpServerProvider.provider();
        final int defBacklog = get(params, int.class, "webBacklog", 0);
        final HttpServerProvider sp = get(params, HttpServerProvider.class, "serverProvider", dsp);
        server = sp.createHttpServer(getInetSocketAddress(params, "webAddress", 8080), defBacklog);
        final ThreadGroup webThreadPoolGroup = new ThreadGroup(threadGroup, id() + ".pool");
        server.setExecutor(new ThreadPoolExecutor(
                get(params, int.class, "webPoolInitSize", 0),
                get(params, int.class, "webPoolMaxSize", 64),
                get(params, long.class, "webPoolKeepAliveTime", 60_000L),
                TimeUnit.MILLISECONDS,
                getBlockingQueue(params, "webPoolBlockingQueue", 64),
                getThreadFactory(params, "webPoolThreadFactory", webThreadPoolGroup,
                        get(params, boolean.class, "webPoolDaemons", false), threadStackSize),
                getRejectedExecutionHandler(params, "webPoolRejectedExecutionHandler"))
        );
        webDir = dirMap.get("default");
        server.createContext("/", this);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            final String[] parts = exchange.getRequestURI().getRawPath().substring(1).split("/");
            Path path = webDir;
            for (final String part : parts) {
                path = path.resolve(URLDecoder.decode(part, "UTF-8"));
            }
            if (Files.isDirectory(path)) {
                for (final String page : defaultPages) {
                    final Path pagePath = path.resolve(page);
                    final HttpHandler handler = handlerMap.get(webDir.relativize(pagePath));
                    if (handler != null) {
                        handler.handle(exchange);
                        return;
                    }
                }
            }
            exchange.sendResponseHeaders(HTTP_NOT_FOUND, 0);
        } catch (Exception x) {
            warning(log, "{0} Unable to handle {1}", this, exchange.getRequestURI());
        } finally {
            exchange.close();
        }
    }

    @Override
    protected List<String> defaultPages(Collection collection) {
        if (collection.isEmpty()) {
            return Arrays.asList("index.groovy", "index.html", "index.svg");
        } else {
            return super.defaultPages(collection);
        }
    }

    @Override
    protected boolean watchFilter(Path path) {
        final Path defaultDir = dirMap.get("default");
        return defaultDir == null || !path.startsWith(defaultDir) || super.watchFilter(path);
    }

    @Override
    protected void onAdd(Path dir, Path context) {
        final String fname = context.getFileName().toString();
        if (fname.startsWith("_")) {
            applyScripts(dir, context, fname);
        } else {
            modify(dir, context);
        }
    }

    @Override
    protected void onModify(Path dir, Path context) {
        final String fname = context.getFileName().toString();
        if (fname.startsWith("_")) {
            applyScripts(dir, context, fname);
        } else {
            if ("text/groovy".equals(fileTypeMap.getContentType(fname))) {
                try {
                    server.removeContext(toContextPath(context));
                    handlerMap.remove(context);
                    info(log, "{0} Removed context {1} {2}", this, dir, context);
                } catch (Exception x) {
                    warning(log, "{0} Unable to remove context {1} {2}", x, this, dir, context);
                }
                modify(dir, context);
            }
        }
    }

    @Override
    protected void onDelete(Path dir, Path context) {
        final String fname = context.getFileName().toString();
        if (fname.startsWith("_")) {
            applyScripts(dir, context, fname);
        } else {
            try {
                server.removeContext(toContextPath(context));
                handlerMap.remove(context);
                info(log, "{0} Removed context {1} {2}", this, dir, context);
            } catch (Exception x) {
                warning(log, "{0} Unable to remove context {1} {2}", x, this, dir, context);
            }
        }
    }

    protected void modify(Path dir, Path context) {
        final String fname = context.getFileName().toString();
        final LinkedList<Path> scriptPaths = new LinkedList<>();
        final String mime = fileTypeMap.getContentType(fname);
        final boolean script = "text/groovy".equals(mime);
        final Path file = dir.resolve(context);
        if (script) {
            scriptPaths.addFirst(file);
        }
        for (Path d = context.getParent(); d != null; d = d.getParent()) {
            scriptPaths.addAll(Arrays.asList(
                    dir.resolve(d).resolve("_" + fname + ".groovy"),
                    dir.resolve(d).resolve("_." + getFileExtension(fname) + ".groovy"),
                    dir.resolve(d).resolve("_.groovy")));
        }
        final Map<Closure, Path> scripts = new LinkedHashMap<>();
        for (final Path path : scriptPaths) {
            if (Files.isRegularFile(path)) {
                try {
                    final GroovyCodeSource source = new GroovyCodeSource(path.toFile());
                    scripts.put(GroovyRuntime.getClosure(source), path);
                } catch (Exception x) {
                    warning(log, "{0} Unable to compile {1}", this, path);
                }
            }
        }
        final String contentType = mime.startsWith("text/") ? mime + ";charset=UTF-8" : mime;
        try {
            final String contextPath = toContextPath(context);
            final HttpHandler handler = new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    if (!script) {
                        exchange.getResponseHeaders().set("Content-Type", contentType);
                    }
                    boolean filtered = false;
                    for (final Entry<Closure, Path> e : scripts.entrySet()) {
                        try {
                            final Object v = e.getKey().call(file, exchange);
                            if (!filtered && Boolean.TRUE.equals(v)) {
                                filtered = true;
                            }
                        } catch (Exception x) {
                            warning(log, "{0} Script {1}", SimpleWebServer.this, e.getValue());
                            if (exchange.getResponseCode() < 0) {
                                exchange.sendResponseHeaders(HTTP_INTERNAL_ERROR, -1);
                                return;
                            }
                        }
                    }
                    if (!script) {
                        if (exchange.getResponseCode() < 0) {
                            exchange.sendResponseHeaders(HTTP_OK, filtered ? 0L : Files.size(file));
                        }
                        try {
                            Files.copy(file, exchange.getResponseBody());
                        } catch (Exception x) {
                            warning(log, "{0} I/O error {1}", SimpleWebServer.this, file);
                        }
                    }
                    exchange.close();
                }
            };
            server.createContext(contextPath, handler);
            handlerMap.put(context, handler);
            info(log, "{0} Bound {1} to {2}", this, contextPath, file);
        } catch (Exception x) {
            warning(log, "{0} Unable to register context {1} {2}", this, dir, context);
        }
    }

    protected void applyScripts(final Path dir, Path context, String fname) {
        final String name = getNameWithoutExtension(fname).substring(1);
        try {
            Files.walkFileTree(dir.resolve(context).getParent(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path f, BasicFileAttributes a) {
                    if (!filterPath(f)) {
                        final boolean modify;
                        if (name.isEmpty()) {
                            modify = true;
                        } else if (name.startsWith(".")) {
                            final String file = f.getFileName().toString();
                            modify = name.substring(1).equals(getFileExtension(file));
                        } else {
                            modify = name.equals(f.getFileName().toString());
                        }
                        if (modify) {
                            modify(dir, dir.relativize(f));
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path d, BasicFileAttributes a) {
                    if (filterPath(d)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }
            });
        } catch (Exception x) {
            warning(log, "{0} Unable to apply scripts {1} {2}", x, this, dir, context);
        }
    }

    @Override
    protected void doStart() {
        try {
            executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (webDir == null) {
                        warning(log, "{0} No default directory found", SimpleWebServer.this);
                        return null;
                    } else {
                        Files.createDirectories(webDir);
                    }
                    server.start();
                    return null;
                }
            }).get();
            notifyStarted();
        } catch (InterruptedException x) {
            warning(log, "{0} Interrupted", this);
            notifyFailed(x);
        } catch (CancellationException x) {
            warning(log, "{0} Cancelled", this);
            notifyFailed(x);
        } catch (ExecutionException x) {
            warning(log, "{0} Start error", x.getCause(), this);
            notifyFailed(x.getCause());
        } catch (Exception x) {
            notifyFailed(x);
        }
    }

    @Override
    protected void doStop() {
        try {
            executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    server.stop(stopTimeout);
                    return null;
                }
            }).get();
            notifyStarted();
        } catch (InterruptedException x) {
            warning(log, "{0} Interrupted", this);
            notifyFailed(x);
        } catch (CancellationException x) {
            warning(log, "{0} Cancelled", this);
            notifyFailed(x);
        } catch (ExecutionException x) {
            warning(log, "{0} Stop error", x.getCause(), this);
            notifyFailed(x.getCause());
        } catch (Exception x) {
            notifyFailed(x);
        }
    }
}
