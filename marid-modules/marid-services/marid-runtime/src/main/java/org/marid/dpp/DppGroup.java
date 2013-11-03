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

import org.marid.tree.StaticTreeObject;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class DppGroup extends DppTask {

    protected final Object prefixFunc;
    protected final Object postfixFunc;

    protected DppGroup(StaticTreeObject parent, String name, Map params) {
        super(parent, name, params);
        prefixFunc = params.get("prefixFunc");
        postfixFunc = params.get("postfixFunc");
        DppUtil.addTasks(logger, this, children, params);
    }

    @Override
    public void start() {
        for (final StaticTreeObject child : children.values()) {
            if (child instanceof DppTask) {
                ((DppTask) child).start();
            }
        }
    }

    @Override
    public void stop() {
        for (final StaticTreeObject child : children.values()) {
            if (child instanceof DppTask) {
                ((DppTask) child).stop();
            }
        }
    }
}
