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

package org.marid.service.proto.pb;

import org.marid.service.proto.ProtoObject;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public class PbContext extends ProtoObject {

    protected final PbService service;

    protected PbContext(PbService service, String name, Map<String, Object> map) {
        super(null, name, map);
        this.service = service;
    }

    public PbService getService() {
        return service;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public ProtoObject getParent() {
        return null;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public boolean isStarted() {
        return false;
    }

    @Override
    public boolean isStopped() {
        return false;
    }

    @Override
    public ProtoObject getContext() {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
