/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.io.serial;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Map;

import static org.marid.pref.PrefCodecs.mapv;

/**
 * @author Dmitry Ovchinnikov
 */
public final class SerialTransceiverParameters {

    private String name = "/dev/ttyS0";
    private long timeout = 10_000L;

    public SerialTransceiverParameters() {
    }

    public SerialTransceiverParameters(Map<String, Object> p) {
        if (p.containsKey("name")) {
            name = mapv(p, "name", String.class);
        }
        if (p.containsKey("timeout")) {
            timeout = mapv(p, "timeout", long.class);
        }
    }

    public String getName() {
        return name;
    }

    public long getTimeout() {
        return timeout;
    }

    public SerialTransceiverParameters setName(String name) {
        this.name = name;
        return this;
    }

    public SerialTransceiverParameters setTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
