/*-
 * #%L
 * marid-runtime
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.runtime.exception;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanInitializationException extends RuntimeException {

    private final String beanName;

    public MaridBeanInitializationException(String beanName, Throwable cause) {
        super(cause);
        this.beanName = beanName;
    }

    public MaridBeanInitializationException(String beanName) {
        this.beanName = beanName;
    }

    @Override
    public String getMessage() {
        return beanName;
    }

    public String getBeanName() {
        return beanName;
    }
}