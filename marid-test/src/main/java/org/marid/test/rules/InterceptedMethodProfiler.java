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

import groovy.lang.BenchmarkInterceptor;
import groovy.lang.Closure;
import groovy.lang.ProxyMetaClass;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.beans.IntrospectionException;
import java.util.Formatter;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class InterceptedMethodProfiler extends MethodProfiler {

    private final ProxyMetaClass proxyMetaClass;
    private final BenchmarkInterceptor interceptor;

    public InterceptedMethodProfiler(Class<?> type) {
        try {
            proxyMetaClass = ProxyMetaClass.getInstance(type);
            proxyMetaClass.setInterceptor(interceptor = new BenchmarkInterceptor());
        } catch (IntrospectionException x) {
            throw new IllegalStateException(x);
        }
    }

    @Override
    protected void invokeStatement(final Statement base, Description description) throws Throwable {
        proxyMetaClass.use(new Closure(this) {
            @Override
            public Object call(Object... args) {
                try {
                    base.evaluate();
                    return null;
                } catch (Throwable x) {
                    throw new IllegalStateException(x);
                }
            }
        });
    }

    @Override
    protected void beforeRule(Statement base, Description description, Map<String, Object> ctx) {
        interceptor.reset();
        super.beforeRule(base, description, ctx);
    }

    protected Formatter data(Statement base, Description description, Map<String, Object> ctx) {
        final long time = System.currentTimeMillis() - (long) ctx.get("start");
        final StringBuilder builder = new StringBuilder();
        final Formatter formatter = new Formatter(builder);
        for (final Object line : interceptor.statistic()) {
            formatter.format("# %s ", description);
            formatter.format("%s count = %d time = %d ms", (Object[]) line);
            formatter.format("%s", System.lineSeparator());
        }
        formatter.format("# %s total time = %d ms %s", description, time, System.lineSeparator());
        return formatter;
    }

    @Override
    protected void afterRule(Statement base, Description description, Map<String, Object> ctx) {
        System.out.print(data(base, description, ctx));
    }
}
