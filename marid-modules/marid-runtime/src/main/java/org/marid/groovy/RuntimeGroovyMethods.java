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

package org.marid.groovy;

import groovy.lang.Closure;
import groovy.lang.IntRange;
import org.marid.Marid;
import org.marid.io.IoContext;
import org.marid.logging.LogSupport;
import org.marid.service.proto.ProtoEventListener;
import org.marid.service.proto.ProtoObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;

import static java.util.logging.Level.*;
import static org.marid.logging.LogSupport.Log.log;

/**
 * @author Dmitry Ovchinnikov.
 */
public class RuntimeGroovyMethods {

    public static void warning(Object object, String message, Throwable thrown, Object... args) {
        log(logger(object), WARNING, message, thrown, args);
    }

    public static void warning(Object object, String message, Object... args) {
        log(logger(object), WARNING, message, null, args);
    }

    public static void info(Object object, String message, Object... args) {
        log(logger(object), INFO, message, null, args);
    }

    public static void severe(Object object, String message, Throwable thrown, Object... args) {
        log(logger(object), SEVERE, message, thrown, args);
    }

    public static void severe(Object object, String message, Object... args) {
        log(logger(object), SEVERE, message, null, args);
    }

    public static void fine(Object object, String message, Object... args) {
        log(logger(object), FINE, message, null, args);
    }

    public static void finer(Object object, String message, Object... args) {
        log(logger(object), FINER, message, null, args);
    }

    public static void finest(Object object, String message, Object... args) {
        log(logger(object), FINEST, message, null, args);
    }

    public static void config(Object object, String message, Object... args) {
        log(logger(object), CONFIG, message, null, args);
    }

    public static ProtoEventListener leftShift(ProtoObject protoObject, Map<String, Closure<Void>> map) {
        final ProtoEventListener eventListener = event -> {
            final Closure<Void> closure = map.get(event.message);
            if (closure != null) {
                closure.call(event);
            }
        };
        protoObject.addEventListener(eventListener);
        return eventListener;
    }

    public static void leftShift(IoContext context, Collection<?> list) throws IOException {
        context.write(list);
    }

    public static Object rightShift(IoContext context, Function<ByteBuffer, Object> function) throws IOException {
        return context.read(function);
    }

    public static IoContext rule(IoContext context, IntRange range, Function<ByteBuffer, Object> f) throws IOException {
        context.rule(buf -> range.contains(buf.capacity()) ? f.apply(buf) : null);
        return context;
    }

    public static IoContext rule(IoContext context, int size, Function<ByteBuffer, Object> f) throws IOException {
        context.rule(buf -> buf.capacity() == size ? f.apply(buf) : null);
        return context;
    }

    private static Logger logger(Object object) {
        if (object instanceof LogSupport) {
            return ((LogSupport) object).logger();
        } else if (object instanceof Closure) {
            return logger(((Closure) object).getOwner());
        } else {
            return Marid.LOGGER;
        }
    }
}
