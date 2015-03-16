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

import org.marid.service.AbstractMaridService;

/**
 * @author Dmitry Ovchinnikov
 */
public class PbService extends AbstractMaridService {

    protected final PbContext context;

    public PbService(PbServiceConfiguration configuration) {
        super(configuration);
        context = new PbContext(this, configuration.name(this), configuration.data(this));
        context.init();
    }

    @Override
    public void start() throws Exception {
        super.start();
        context.start();
    }

    @Override
    public void close() throws Exception {
        context.close();
        super.close();
    }

    public PbContext getContext() {
        return context;
    }
}
