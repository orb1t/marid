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

import org.marid.runtime.beans.Bean;
import org.marid.runtime.beans.BeanMethod;
import org.marid.runtime.beans.BeanMethodArg;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridBeanArgConverterNotFoundException extends RuntimeException {

    private final String beanName;
    private final String methodName;
    private final int argIndex;
    private final String type;

    public MaridBeanArgConverterNotFoundException(String beanName, String methodName, int argIndex, String type) {
        this.beanName = beanName;
        this.methodName = methodName;
        this.argIndex = argIndex;
        this.type = type;
    }

    public MaridBeanArgConverterNotFoundException(Bean bean, BeanMethod method, BeanMethodArg arg) {
        this(bean.name, method.signature, arg.index(method), arg.type);
    }

    public String getBeanName() {
        return beanName;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getArgIndex() {
        return argIndex;
    }

    public String getType() {
        return type;
    }

    @Override
    public String getMessage() {
        return String.format("%s.%s(%s:%s)", beanName, methodName, argIndex, type);
    }
}
