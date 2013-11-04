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

import org.marid.methods.PropMethods;
import org.marid.service.AbstractMaridService;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class DppService extends AbstractMaridService {

    protected final DppScheduler scheduler;

    public DppService(Map params) {
        super(params);
        scheduler = new DppScheduler(id, PropMethods.get(params, Map.class, "params", emptyMap()));
    }

    @Override
    protected Object processMessage(Object message) throws Exception {
        return null;
    }

    @Override
    protected void doStart() {
        try {
            scheduler.start();
            notifyStarted();
        } catch (Exception x) {
            notifyFailed(x);
        }
    }

    @Override
    protected void doStop() {
        try {
            scheduler.stop();
            notifyStopped();
        } catch (Exception x) {
            notifyFailed(x);
        }
    }
}
