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

package org.marid.service.proto.pp;

import org.marid.dyn.MetaInfo;
import org.marid.service.AbstractMaridService;

/**
 * @author Dmitry Ovchinnikov
 */
@MetaInfo(name = "Protocol Parser Service", description = "Protocol Parser Service")
public class PpService extends AbstractMaridService {

    protected final PpContext context;

    public PpService(PpServiceConfiguration configuration) {
        super(configuration);
        context = new PpContext(this, configuration.name(this), configuration.data());
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

    public PpContext getContext() {
        return context;
    }
}
