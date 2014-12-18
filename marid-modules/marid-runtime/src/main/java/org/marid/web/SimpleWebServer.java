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
import org.marid.dyn.MetaInfo;
import org.marid.groovy.GroovyRuntime;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import static java.net.HttpURLConnection.*;
import static java.util.Collections.addAll;
import static org.marid.nio.FileUtils.extension;
import static org.marid.nio.FileUtils.fileNameWithoutExtension;

/**
 * @author Dmitry Ovchinnikov
 */
@MetaInfo(icon = "services/web.png", name = "Simple web server", description = "Simple threaded web server")
public class SimpleWebServer extends AbstractWebServer implements HttpHandler {

    protected final HttpServer server;
    protected final Map<Path, HttpHandler> handlerMap = new ConcurrentHashMap<>();
    protected final int stopTimeout;

    public SimpleWebServer(SimpleWebServerConfig conf) throws IOException {
        super(conf);
        stopTimeout = conf.stopTimeout();
        final InetSocketAddress address = new InetSocketAddress(conf.host(), conf.port());
        server = conf.secure()
                ? HttpServerProvider.provider().createHttpsServer(address, conf.backlog())
                : HttpServerProvider.provider().createHttpServer(address, conf.backlog());
        server.setExecutor(executor);
        server.createContext("/", this);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            final String[] parts = exchange.getRequestURI().getRawPath().substring(1).split("/");
            Path path = getWebDir();
            for (final String part : parts) {
                path = path.resolve(URLDecoder.decode(part, "UTF-8"));
            }
            if (Files.isDirectory(path)) {
                for (final String page : defaultPages) {
                    final Path pagePath = path.resolve(page);
                    final HttpHandler handler = handlerMap.get(getWebDir().relativize(pagePath));
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

    public InetSocketAddress getAddress() {
        return server.getAddress();
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
            addAll(scriptPaths,
                    dir.resolve(d).resolve("_" + fname + ".groovy"),
                    dir.resolve(d).resolve("_." + extension(fname) + ".groovy"),
                    dir.resolve(d).resolve("_.groovy"));
        }
        final Map<BiFunction, Path> scripts = new LinkedHashMap<>();
        scriptPaths.stream().filter(path -> Files.isRegularFile(path)).forEach(path -> {
            try {
                try (final Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    scripts.put((BiFunction) GroovyRuntime.SHELL.parse(reader).run(), path);
                }
            } catch (Exception x) {
                warning("{0} Unable to compile {1}", this, path);
            }
        });
        final String contentType = mime.startsWith("text/") ? mime + ";charset=UTF-8" : mime;
        try {
            final String contextPath = toContextPath(context);
            final HttpHandler handler = exchange -> {
                try {
                    if (!script) {
                        exchange.getResponseHeaders().set("Content-Type", contentType);
                    }
                    boolean filtered = false;
                    for (final Entry<BiFunction, Path> e : scripts.entrySet()) {
                        try {
                            @SuppressWarnings("unchecked")
                            final Object v = e.getKey().apply(file, exchange);
                            if (!filtered && Boolean.TRUE.equals(v)) {
                                filtered = true;
                            }
                        } catch (Exception x) {
                            warning("{0} Script {1}", x, SimpleWebServer.this, e.getValue());
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
                } finally {
                    exchange.close();
                }
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

    public Path getWebDir() {
        return dirMap.get("default");
    }

    @Override
    @PostConstruct
    public void start() throws Exception {
        super.start();
        if (getWebDir() == null) {
            warning("{0} No default directory found", SimpleWebServer.this);
            return;
        } else {
            Files.createDirectories(getWebDir());
        }
        server.start();
    }

    @Override
    public void close() throws Exception {
        try {
            info("Stopping server in {0} s", stopTimeout);
            server.stop(stopTimeout);
            info("Server was stopped");
        } finally {
            super.close();
        }
    }
}
