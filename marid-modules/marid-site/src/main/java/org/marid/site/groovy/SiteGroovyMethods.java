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

package org.marid.site.groovy;

import com.sun.net.httpserver.HttpExchange;
import groovy.lang.Script;
import groovy.xml.MarkupBuilder;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov.
 */
public class SiteGroovyMethods {

    public static BiFunction web(Script script, BiFunction<Path, HttpExchange, Object> function) {
        return function;
    }

    public static void sendResponseHeaders(HttpExchange exchange, String code, long size) throws IOException {
        try {
            final Field field = HttpURLConnection.class.getField("HTTP_" + code.toUpperCase());
            if (field.getType() == int.class) {
                exchange.sendResponseHeaders(field.getInt(null), size);
            } else {
                throw new IllegalArgumentException(code);
            }
        } catch (NoSuchFieldException | IllegalAccessException x) {
            throw new IllegalArgumentException(code, x);
        }
    }

    public static void withMarkupBuilder(HttpExchange ex, Consumer<MarkupBuilder> consumer) throws IOException {
        try (final Writer writer = new OutputStreamWriter(ex.getResponseBody(), StandardCharsets.UTF_8)) {
            final MarkupBuilder markupBuilder = new MarkupBuilder(writer);
            consumer.accept(markupBuilder);
        }
    }
}
