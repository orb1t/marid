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
import org.marid.service.AbstractMaridService;

import javax.activation.FileTypeMap;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.io.Files.getFileExtension;
import static com.google.common.io.Files.getNameWithoutExtension;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.util.Collections.singletonList;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.join;
import static org.marid.groovy.GroovyRuntime.get;
import static org.marid.methods.LogMethods.info;
import static org.marid.methods.LogMethods.warning;
import static org.marid.proputil.PropUtil.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class WebServer extends AbstractMaridService implements Runnable {

    private static final Kind<?>[] ALL_KINDS = {ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY};
    protected static final Pattern EXT_PATTERN = Pattern.compile("_[.](\\p{Alnum}+)[.]groovy");

    private final HttpServer httpServer;
    private final int stopTimeout;
    private final Path dir;
    private final WatchService watchService;
    private final FileTypeMap fileTypeMap;
    private final Thread watchServiceThread;

    public WebServer(Map params) throws IOException {
        super(params);
        final Path userHome = Paths.get(System.getProperty("user.home"));
        dir = get(Path.class, params, "dir", userHome.resolve("marid").resolve("web"));
        stopTimeout = get(int.class, params, "stopTimeout", 60);
        final HttpServerProvider dsp = HttpServerProvider.provider();
        final int defaultBacklog = get(int.class, params, "webBacklog", 0);
        final HttpServerProvider sp = get(HttpServerProvider.class, params, "httpServerProvider", dsp);
        httpServer = sp.createHttpServer(getInetSocketAddress(params, "webAddress", 8080), defaultBacklog);
        final ThreadGroup webThreadPoolGroup = new ThreadGroup(threadGroup, id() + ".pool");
        httpServer.setExecutor(new ThreadPoolExecutor(
                get(int.class, params, "webPoolInitSize", 0),
                get(int.class, params, "webPoolMaxSize", 64),
                get(long.class, params, "webPoolKeepAliveTime", 60_000L),
                TimeUnit.MILLISECONDS,
                getBlockingQueue(params, "webPoolBlockingQueue", 64),
                getThreadFactory(params, "webPoolThreadFactory", webThreadPoolGroup,
                        get(boolean.class, params, "webPoolDaemons", false), threadStackSize),
                getRejectedExecutionHandler(params, "webPoolRejectedExecutionHandler"))
        );
        watchService = FileSystems.getDefault().newWatchService();
        fileTypeMap = FileTypeMap.getDefaultFileTypeMap();
        watchServiceThread = new Thread(this);
        addListener(new Listener() {
            @Override
            public void running() {
                watchServiceThread.start();
            }
        }, sameThreadExecutor);
    }

    @Override
    public void run() {
        try (final WatchService wc = watchService) {
            while (isRunning()) {
                final WatchKey key = wc.poll(timeGranularity, TimeUnit.MILLISECONDS);
                if (key == null) {
                    continue;
                }
                try {
                    for (final WatchEvent event : key.pollEvents()) {
                        final String name = ((Path) event.context()).getFileName().toString();
                        if (name.startsWith(".") || name.endsWith(".new")) {
                            continue;
                        }
                        try {
                            final Path path = dir.resolve((Path) event.context());
                            if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                                onModify(path);
                            } else if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                onAdd(path);
                            } else if (event.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                                onDelete(path);
                            } else {
                                throw new IllegalStateException("Unknown event kind: " + event.kind());
                            }
                        } catch (Exception x) {
                            warning(log, "{0} Watch service process {1} error", x, this, event);
                        }
                    }
                } finally {
                    key.reset();
                }
            }
        } catch (InterruptedException x) {
            info(log, "{0} Watch service interrupted", this);
        } catch (IOException x) {
            warning(log, "{0} Watch service I/O error", x, this);
        }
    }

    @Override
    protected Object processMessage(Object message) throws Exception {
        throw new IllegalArgumentException("Unsupported message: " + message);
    }

    @Override
    protected void doStart() {
        try {
            executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    if (!Files.isDirectory(dir)) {
                        Files.createDirectories(dir);
                    }
                    Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            if (!file.getFileName().toString().startsWith("_")) {
                                onAdd(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            if (!dir.getFileName().toString().startsWith("_")) {
                                dir.register(watchService, ALL_KINDS);
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    });
                    httpServer.start();
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

    protected void onModify(Path path) {
        if (!refresh(path)) {
            onDelete(path);
            onAdd(path);
        }
    }

    protected void onAdd(Path path) {
        if (!refresh(path)) {
            register(path);
            info(log, "{0} Added context {1}", this, path);
        }
    }

    protected void onDelete(Path path) {
        if (!refresh(path)) {
            try {
                httpServer.removeContext(toContextPath(dir.relativize(path)));
            } catch (IllegalArgumentException x) {
                warning(log, "{0} Handler for {1} not exists", this, path);
            }
            info(log, "{0} Removed context {1}", this, path);
        }
    }

    protected boolean refresh(Path path) {
        final String name = path.getFileName().toString();
        if (name.equals("_.groovy")) {
            try {
                Files.walkFileTree(path.getParent(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!file.getFileName().toString().startsWith("_")) {
                            onModify(file);
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException x) {
                warning(log, "{0} Apply rule I/O error", x, this);
            }
            return true;
        } else {
            final Matcher matcher = EXT_PATTERN.matcher(name);
            if (matcher.matches()) {
                final String ext = matcher.group(1);
                try {
                    Files.walkFileTree(path.getParent(), new SimpleFileVisitor<Path>() {
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            final String fname = file.getFileName().toString();
                            if (!fname.startsWith("_") && fname.endsWith("." + ext)) {
                                onModify(file);
                            }
                            return FileVisitResult.CONTINUE;
                        }

                        @Override
                        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                    });
                } catch (IOException x) {
                    warning(log, "{0} Apply rule I/O error", x, this);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void closure(Path path, String ext, LinkedList<Closure> closures) throws IOException {
        final Path extScript = path.resolve("_." + ext + ".groovy");
        final Path allScript = path.resolve("_.groovy");
        if (Files.exists(extScript)) {
            closures.addFirst(GroovyRuntime.getClosure(new GroovyCodeSource(extScript.toFile())));
        }
        if (Files.exists(allScript)) {
            closures.addFirst(GroovyRuntime.getClosure(new GroovyCodeSource(allScript.toFile())));
        }
        final Path parent = path.getParent();
        if (parent != null && !dir.equals(path)) {
            closure(parent, ext, closures);
        }
    }

    protected void register(Path path) {
        final String fileName = path.getFileName().toString();
        final String ext = getFileExtension(fileName);
        try {
            final String name = getNameWithoutExtension(fileName);
            final Path satellite = path.getParent().resolve("_" + name + ".groovy");
            final LinkedList<Closure> cs = new LinkedList<>();
            if (Files.exists(satellite)) {
                cs.add(GroovyRuntime.getClosure(new GroovyCodeSource(satellite.toFile())));
            }
            closure(path.getParent(), ext, cs);
            final String mime = fileTypeMap.getContentType(fileName);
            final boolean script;
            if ("text/groovy".equals(mime)) {
                cs.add(GroovyRuntime.getClosure(new GroovyCodeSource(path.toFile())));
                script = true;
            } else {
                script = false;
            }
            final String context = toContextPath(dir.relativize(path));
            if (cs.isEmpty()) {
                httpServer.createContext(context, new FinalDataHttpHandler(path));
                info(log, "{0} Register plain context {1}", this, path);
            } else {
                httpServer.createContext(context, new ScriptedDataHttpHandler(path, cs, script));
                info(log, "{0} Register scripted context {1}", this, path);
            }
        } catch (Exception x) {
            warning(log, "{0} Unable to register {1}", this, path);
        }
    }

    @SuppressWarnings("unchecked")
    protected String toContextPath(Path path) {
        final String name = path.getFileName().toString();
        final String mime = fileTypeMap.getContentType(name);
        if ("text/groovy".equals(mime)) {
            path = path.getNameCount() == 1
                ? Paths.get(getNameWithoutExtension(name) + ".msp")
                : path.getParent().resolve(getNameWithoutExtension(name) + ".msp");
        }
        return "/" + join((Iterator) path.iterator(), "/");
    }

    @Override
    protected void doStop() {
        try {
            executor.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    httpServer.stop(stopTimeout);
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

    protected class FinalDataHttpHandler implements HttpHandler {

        protected final Path path;
        protected final long size;
        protected final List<String> contentType;

        public FinalDataHttpHandler(Path path) throws IOException {
            this.path = path;
            size = Files.size(path);
            contentType = singletonList(fileTypeMap.getContentType(path.getFileName().toString()));
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {
                exchange.getResponseHeaders().put("Content-Type", contentType);
                exchange.sendResponseHeaders(HTTP_OK, size);
                Files.copy(path, exchange.getResponseBody());
            } catch (Exception x) {
                warning(log, "{0} Unable to process {1} path", WebServer.this, path);
            }
        }
    }

    protected class ScriptedDataHttpHandler implements HttpHandler {

        protected final Path path;
        protected final List<Closure> closures;
        protected final boolean script;

        public ScriptedDataHttpHandler(Path path, List<Closure> closures, boolean script) {
            this.path = path;
            this.closures = closures;
            this.script = script;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!script) {
                if (exchange.getRequestHeaders().containsKey("Content-Type")) {
                    final String mime = fileTypeMap.getContentType(path.getFileName().toString());
                    final List<String> type = Collections.singletonList(mime);
                    exchange.getResponseHeaders().put("Content-Type", type);
                }
            }
            boolean filtered = false;
            try {
                for (final Closure closure : closures) {
                    final Object v = closure.call(path, exchange);
                    if (!filtered && Boolean.TRUE.equals(v)) {
                        filtered = true;
                    }
                }
            } catch (Exception x) {
                warning(log, "{0} Unable to process {1} path", WebServer.this, path);
            }
            if (!script) {
                exchange.sendResponseHeaders(HTTP_OK, filtered ? 0L : Files.size(path));
                try {
                    Files.copy(path, exchange.getResponseBody());
                } catch (Exception x) {
                    warning(log, "{0} Unable to copy streams for {1}", WebServer.this, path);
                }
            }
        }
    }
}
