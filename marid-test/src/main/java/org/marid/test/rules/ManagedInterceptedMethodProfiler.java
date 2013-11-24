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

package org.marid.test.rules;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Formatter;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class ManagedInterceptedMethodProfiler extends InterceptedMethodProfiler {

    public ManagedInterceptedMethodProfiler(Class<?> type) {
        super(type);
    }

    @Override
    protected void beforeRule(Statement base, Description description, final Map<String, Object> ctx) {
        long collectionCount = 0L;
        for (final GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            collectionCount += gcBean.getCollectionCount();
        }
        ctx.put("initialCollections", collectionCount);
        super.beforeRule(base, description, ctx);
    }

    @Override
    protected Formatter data(Statement base, Description description, Map<String, Object> ctx) {
        final Formatter formatter = super.data(base, description, ctx);
        long collectionCount = 0L;
        for (final GarbageCollectorMXBean gcBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            collectionCount += gcBean.getCollectionCount();
        }
        final long cls = collectionCount - (long) ctx.get("initialCollections");
        formatter.format("# %s memory collections = %d%s", description, cls, System.lineSeparator());
        return formatter;
    }
}
