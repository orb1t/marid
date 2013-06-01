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

package org.marid.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Service extends ConcurrentMap<String, Object> {

    public String getName();

    public String getType();

    public String getVersion();

    public String getLabel();

    public void start() throws Exception;

    public void stop() throws Exception;

    public boolean isRunning();

    public ThreadGroup getThreadGroup();

    public Future<Map<String, Object>> send(Map<String, Object> message);

    public List<Future<Map<String, Object>>> send(Collection<Map<String, Object>> messages);
}
