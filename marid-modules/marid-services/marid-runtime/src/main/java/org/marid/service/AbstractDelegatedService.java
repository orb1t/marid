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

package org.marid.service;

import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractDelegatedService extends AbstractService implements DelegatedService {

    protected final String delegateId;

    public AbstractDelegatedService(Map<String, Object> params) {
        super(params);
        if (params.containsKey("delegateId")) {
            delegateId = String.valueOf(params.get("delegateId"));
        } else {
            throw new IllegalArgumentException("No delegateId found in " + params);
        }
    }

    @Override
    public Service delegate() {
        Service d = ServiceMappers.getServiceMapper().getService(delegateId);
        if (d == null) {
            throw new NullPointerException(this + ": No delegate found");
        } else {
            return d;
        }
    }
}
