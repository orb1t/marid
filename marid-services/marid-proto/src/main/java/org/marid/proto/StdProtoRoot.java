/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.proto;

import org.marid.io.IOSupplier;

import java.nio.channels.ByteChannel;
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
    public Map<String, StdProtoBus> getChildren() {
        return children;
    }

    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }

    public StdProtoBus bus(String id, String name, IOSupplier<ByteChannel> channelProvider, StdProtoBusProps props) {
        return new StdProtoBus(this, id, name, channelProvider, props);
    }
}
