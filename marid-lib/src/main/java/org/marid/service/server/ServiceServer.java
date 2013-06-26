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

package org.marid.service.server;

import org.marid.service.AbstractDelegatedService;
import org.marid.service.Transaction;
import org.marid.service.data.Request;
import org.marid.service.data.Response;
import org.marid.service.ServiceDescriptor;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * @author Dmitry Ovchinnikov
 */
public class ServiceServer extends AbstractDelegatedService {

    public ServiceServer(String id, String type, ServiceDescriptor descriptor) {
        super(id, type, descriptor);
    }

    @Override
    protected void doStart() throws Exception {
    }

    @Override
    protected void doStop() throws Exception {
    }

    @Override
    protected <T extends Response> Future<T> doSend(Request<T> message) {
        return delegate().send(message);
    }

    @Override
    protected Transaction doTransaction(Map<String, Object> params) {
        return delegate().transaction(params);
    }
}
