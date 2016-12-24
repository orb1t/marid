/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.ide.common;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Component
public class IdeUrlHandlerFactory implements URLStreamHandlerFactory {

    private final Map<String, URLStreamHandler> handlerMap = new HashMap<>();

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        return handlerMap.get(protocol);
    }

    public void register(String protocol, URLStreamHandler handler) {
        handlerMap.put(protocol, handler);
    }

    public URLStreamHandler unregister(String protocol) {
        return handlerMap.remove(protocol);
    }

    @PostConstruct
    private void init() {
        URL.setURLStreamHandlerFactory(this);
    }
}
