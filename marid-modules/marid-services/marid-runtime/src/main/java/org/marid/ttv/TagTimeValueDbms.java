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

package org.marid.ttv;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
public interface TagTimeValueDbms extends AutoCloseable {

    public void start();

    Set<String> tagSet();

    Date getMinTimestamp(String tag);

    Date getMaxTimestamp(String tag);

    void setMaximumFetchSize(int size);

    int getMaximumFetchSize();

    void setQueryTimeout(int timeout);

    int getQueryTimeout();

    <T> Map<String, Map<Date, T>> after(Class<T> type, Set<String> tags, Date from, boolean inc);

    <T> Map<String, Map<Date, T>> before(Class<T> type, Set<String> tags, Date to, boolean inc);

    <T> Map<String, Map<Date, T>> between(Class<T> type, Set<String> tags, Date from, boolean fromInc, Date to, boolean toInc);

    long usedSize();

    <T> int insert(Class<T> type, Map<String, Map<Date, T>> data);

    <T> int insertOrUpdate(Class<T> type, Map<String, Map<Date, T>> data);

    <T> int update(Class<T> type, Map<String, Map<Date, T>> data);

    int remove(Class<?> type, Set<String> tags);

    int removeAfter(Class<?> type, Set<String> tags, Date from, boolean inc);

    int removeBefore(Class<?> type, Set<String> tags, Date to, boolean inc);

    int remove(Class<?> type, Set<String> tags, Date from, boolean fromInc, Date to, boolean toInc);

    int remove(Class<?> type, Map<String, Date> keys);

    void clear();

    Set<Class<?>> supportedTypes();
}
