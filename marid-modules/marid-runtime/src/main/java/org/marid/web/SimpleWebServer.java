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
import groovy.lang.Closure;
import groovy.lang.GroovyCodeSource;
import org.marid.dyn.MetaInfo;
import org.marid.groovy.GroovyRuntime;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.net.HttpURLConnection.*;
import static org.marid.nio.FileUtils.extension;
import static org.marid.nio.FileUtils.fileNameWithoutExtension;

/**
 * @author Dmitry Ovchinnikov
 */
@MetaInfo(icon = "services/web.png", name = "Simple web server", description = "Simple threaded web server")
public class SimpleWebServer extends AbstractWebServer implements HttpHandler {

    public final ThreadGroup webPoolThreadGroup = new ThreadGroup(threadPoolGroup, "webPool");
    protected final HttpServer server;
    protected final Path webDir;
    protected final Map<Path, HttpHandler> handlerMap = new ConcurrentHashMap<>();

    public SimpleWebServer(SimpleWebServerParameters params) throws IOException {
        super(params);
        server = params.httpServerProvider.createHttpServer(params.address, params.backlog);
        server.setExecutor(new ThreadPoolExecutor(
                params.webThreadPoolInitSize,
                params.webThreadPoolMaxSize,
                params.webThreadPoolKeepAliveTime,
                TimeUnit.MILLISECONDS,
                params.webBlockingQueueSupplier.get(),
                params.webPoolThreadFactory.apply(this),
                params.webRejectedExecutionHandler.apply(this))
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
            warning("{0} Unable to handle {1}", this, exchange.getRequestURI());
        } finally {
            exchange.close();
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
                    info("{0} Removed context {1} {2}", this, dir, context);
                } catch (Exception x) {
                    warning("{0} Unable to remove context {1} {2}", x, this, dir, context);
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
                info("{0} Removed context {1} {2}", this, dir, context);
            } catch (Exception x) {
                warning("{0} Unable to remove context {1} {2}", x, this, dir, context);
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
                    dir.resolve(d).resolve("_." + extension(fname) + ".groovy"),
                    dir.resolve(d).resolve("_.groovy")));
        }
        final Map<Closure, Path> scripts = new LinkedHashMap<>();
        for (final Path path : scriptPaths) {
            if (Files.isRegularFile(path)) {
                try {
                    final GroovyCodeSource source = new GroovyCodeSource(path.toFile());
                    scripts.put(GroovyRuntime.getClosure(source), path);
                } catch (Exception x) {
                    warning("{0} Unable to compile {1}", this, path);
                }
            }
        }
        final String contentType = mime.startsWith("text/") ? mime + ";charset=UTF-8" : mime;
        try {
            final String contextPath = toContextPath(context);
            final HttpHandler handler = exchange -> {
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
                        warning("{0} Script {1}", SimpleWebServer.this, e.getValue());
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
                        warning("{0} I/O error {1}", SimpleWebServer.this, file);
                    }
                }
                exchange.close();
            };
            server.createContext(contextPath, handler);
            handlerMap.put(context, handler);
            info("{0} Bound {1} to {2}", this, contextPath, file);
        } catch (Exception x) {
            warning("{0} Unable to register context {1} {2}", this, dir, context);
        }
    }

    protected void applyScripts(final Path dir, Path context, String fname) {
        final String name = fileNameWithoutExtension(fname).substring(1);
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
                            modify = name.substring(1).equals(extension(file));
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
            warning("{0} Unable to apply scripts {1} {2}", x, this, dir, context);
        }
    }

    @Override
    public void start() throws Exception {
        super.start();
        executor.submit(() -> {
            if (webDir == null) {
                warning("{0} No default directory found", SimpleWebServer.this);
                return null;
            } else {
                Files.createDirectories(webDir);
            }
            server.start();
            return null;
        }).get();
    }

    @Override
    public void close() throws Exception {
        try {
            executor.submit(() -> {
                server.stop((int) shutdownTimeout);
                return null;
            }).get();
        } finally {
            super.close();
        }
    }
}
