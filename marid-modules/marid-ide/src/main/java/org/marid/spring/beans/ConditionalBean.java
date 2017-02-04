/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.spring.beans;

import org.marid.misc.Casts;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public class ConditionalBean<T> implements FactoryBean<T> {

    private final Class<T> type;
    private final T bean;

    public ConditionalBean(Class<T> type, T bean) {
        this.type = type;
        this.bean = bean;
    }

    public ConditionalBean(T bean) {
        this(bean == null ? null : Casts.cast(bean.getClass()), bean);
    }

    @Override
    public T getObject() throws Exception {
        return bean;
    }

    @Override
    public Class<?> getObjectType() {
        return bean == null ? null : type;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
