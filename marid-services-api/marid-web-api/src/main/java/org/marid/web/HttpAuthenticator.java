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

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.HttpExchange;

import javax.annotation.Nonnull;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov.
 */
public final class HttpAuthenticator extends Authenticator {

    private final Function<HttpExchange, Authenticator.Result> authFunction;

    public HttpAuthenticator(@Nonnull Function<HttpExchange, Authenticator.Result> authFunction) {
        this.authFunction = authFunction;
    }

    @Override
    public Result authenticate(HttpExchange httpExchange) {
        return authFunction.apply(httpExchange);
    }
}
