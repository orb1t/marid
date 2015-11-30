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

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;
import org.jmlspecs.annotation.Immutable;
import org.marid.io.IOBiConsumer;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author Dmitry Ovchinnikov.
 */
@Immutable
public final class HttpFilter extends Filter {

    private final String description;
    private final IOBiConsumer<HttpExchange, Chain> filter;

    public HttpFilter(@Nonnull String description, @Nonnull IOBiConsumer<HttpExchange, Chain> filter) {
        this.description = description;
        this.filter = filter;
    }

    @Override
    public void doFilter(HttpExchange httpExchange, Chain chain) throws IOException {
        filter.ioAccept(httpExchange, chain);
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", getClass().getSimpleName(), description);
    }
}
