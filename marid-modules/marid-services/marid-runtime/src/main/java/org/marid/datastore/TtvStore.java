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

package org.marid.datastore;

import org.marid.io.SafeResult;
import org.marid.service.Parameterized;

import java.util.Date;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
public interface TtvStore extends Parameterized {

    SafeResult<Set<String>> tagSet(String pattern);

    SafeResult<Map<String, Date>> getMinTimestamp(Class<?> type, Set<String> tags);

    SafeResult<Map<String, Date>> getMaxTimestamp(Class<?> type, Set<String> tags);

    <T> SafeResult<Map<String, NavigableMap<Date, T>>> after(Class<T> type, Set<String> tags, Date from, boolean inc);

    <T> SafeResult<Map<String, NavigableMap<Date, T>>> before(Class<T> type, Set<String> tags, Date to, boolean inc);

    <T> SafeResult<Map<String, NavigableMap<Date, T>>> between(Class<T> type, Set<String> tags, Date from, boolean fromInc, Date to, boolean toInc);

    long usedSize();

    <T> SafeResult<Long> insert(Class<T> type, Map<String, ? extends Map<? extends Date, T>> data);

    <T> SafeResult<Long> insertOrUpdate(Class<T> type, Map<String, ? extends Map<? extends Date, T>> data);

    <T> SafeResult<Long> update(Class<T> type, Map<String, ? extends Map<? extends Date, T>> data);

    SafeResult<Long> remove(Class<?> type, Set<String> tags);

    SafeResult<Long> removeAfter(Class<?> type, Set<String> tags, Date from, boolean inc);

    SafeResult<Long> removeBefore(Class<?> type, Set<String> tags, Date to, boolean inc);

    SafeResult<Long> removeBetween(Class<?> type, Set<String> tags, Date from, boolean fromInc, Date to, boolean toInc);

    SafeResult<Long> removeKeys(Class<?> type, Map<String, ? extends Date> keys);

    SafeResult<Long> clear();

    boolean isTypeSupported(Class<?> type);
}
