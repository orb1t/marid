/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.groovy;

import groovy.lang.Script;
import org.marid.Marid;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

/**
 * @author Dmitry Ovchinnikov.
 */
public abstract class MaridScript extends Script implements LogSupport {

    public MaridScript() {
        if (Marid.getCurrentContext().isActive()) {
            try {
                final AutowireCapableBeanFactory beanFactory = Marid.getCurrentContext().getBeanFactory();
                beanFactory.autowireBean(this);
            } catch (Exception x) {
                warning("Unable to inject fields", x);
            }
        }
    }
}
