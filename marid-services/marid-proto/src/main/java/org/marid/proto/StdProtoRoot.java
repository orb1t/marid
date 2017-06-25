/*-
 * #%L
 * marid-proto
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.proto;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class StdProtoRoot extends StdProto implements ProtoRoot {

    private final Map<String, StdProtoBus> children = new LinkedHashMap<>();
    private final ThreadGroup threadGroup;

    public StdProtoRoot(String id, String name) {
        super(id, name);
        this.threadGroup = new ThreadGroup(id);
    }

    @Override
    public Map<String, StdProtoBus> getItems() {
        return children;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }
}
