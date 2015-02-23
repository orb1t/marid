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

import org.marid.spring.SpringUtils;

import javax.annotation.PostConstruct;
import java.util.concurrent.ThreadFactory;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractMaridService implements MaridService {

    protected final ThreadGroup threadGroup;
    protected final ThreadFactory threadFactory;
    protected final String name;

    public AbstractMaridService(MaridServiceConfiguration configuration) {
        name = SpringUtils.beanName(getClass());
        threadGroup = new ThreadGroup(getName());
        threadFactory = configuration.threadFactory(this);
    }

    @Override
    public ThreadGroup threadGroup() {
        return threadGroup;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @PostConstruct
    public void start() throws Exception {
    }

    @Override
    public boolean isRunning() {
        return threadGroup.activeCount() > 0;
    }

    @Override
    public String toString() {
        return getName();
    }
}
