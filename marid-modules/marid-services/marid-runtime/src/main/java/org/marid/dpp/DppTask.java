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

package org.marid.dpp;

import org.marid.ParameterizedException;
import org.marid.groovy.ClosureChain;
import org.marid.tree.StaticTreeObject;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.marid.methods.LogMethods.finest;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class DppTask extends StaticTreeObject implements Runnable {

    protected final ClosureChain func;

    protected DppTask(StaticTreeObject parent, String name, Map params) {
        super(parent, name, params);
        func = new ClosureChain(DppFuncs.func(funcs(params.get("func"))));
    }

    protected LinkedList<Object> funcs(Object f) {
        final LinkedList<Object> list = new LinkedList<>();
        if (f != null) {
            list.add(f);
        }
        for (StaticTreeObject o = parent; o instanceof DppGroup; o = o.parent()) {
            final DppGroup g = (DppGroup) o;
            if (g.prefixFunc != null) {
                list.addFirst(g.prefixFunc);
            }
            if (g.postfixFunc != null) {
                list.addLast(g.postfixFunc);
            }
        }
        return list;
    }

    @Override
    public void run() {
        try {
            final Object o = func.call(logger, this, new HashMap<>());
            finest(logger, "Result: {0}", o);
        } catch (ParameterizedException x) {
            if (x.getCause() != null) {
                warning(logger, x.getMessage(), x.getCause(), x.getArgs());
            } else {
                warning(logger, x.getMessage(), x.getArgs());
            }
        } catch (Exception x) {
            warning(logger, "Run error", x);
        }
    }
}
