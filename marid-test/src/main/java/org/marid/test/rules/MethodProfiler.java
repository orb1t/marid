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

import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class MethodProfiler {

    @Rule
    public final TestRule rule = new TestRule() {
        @Override
        public Statement apply(final Statement base, final Description description) {
            return new Statement() {
                @Override
                public void evaluate() throws Throwable {
                    final Map<String, Object> context = new HashMap<>();
                    beforeRule(base, description, context);
                    invokeStatement(base, description);
                    afterRule(base, description, context);
                }
            };
        }
    };

    protected void beforeRule(Statement base, Description description, Map<String, Object> context) {
        context.put("start", System.currentTimeMillis());
    }

    protected void afterRule(Statement base, Description description, Map<String, Object> context) {
        final float time = (System.currentTimeMillis() - (long) context.get("start")) / 1.0e3f;
        System.out.format("%s in %8.3f s%s", description, time, System.lineSeparator());
    }

    protected void invokeStatement(Statement base, Description description) throws Throwable {
        base.evaluate();
    }
}
