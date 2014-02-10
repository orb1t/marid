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

package org.marid.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.marid.dyn.TypeCaster.TYPE_CASTER;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class ParameterizedMaridService extends AbstractMaridService implements Parameterized {

    protected final ConcurrentMap<String, Object> paramMap = new ConcurrentHashMap<>();

    public ParameterizedMaridService(Map params) {
        super(params);
    }

    @Override
    public <T> T getParameter(Class<T> type, String parameter, T defaultValue) {
        final T value = getParameter(type, parameter);
        return value == null ? defaultValue : value;
    }

    @Override
    public <T> T getParameter(Class<T> type, String parameter) {
        final Object value = paramMap.get(parameter);
        return value == null ? null : TYPE_CASTER.cast(type, value);
    }

    @Override
    public void setParameter(String parameter, Object value) {
        paramMap.put(parameter, value);
    }
}
