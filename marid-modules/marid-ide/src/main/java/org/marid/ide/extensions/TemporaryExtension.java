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

package org.marid.ide.extensions;

import org.jboss.weld.context.AbstractContext;
import org.marid.ide.temp.Temporary;
import org.marid.logging.LogSupport;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

/**
 * @author Dmitry Ovchinnikov
 */
public class TemporaryExtension implements Extension, LogSupport {

    public void onTemporaryClean(@Observes TemporaryClean event, BeanManager beanManager) {
        for (final Bean<?> bean : beanManager.getBeans(Temporary.class)) {
            try {
                final AbstractContext context = (AbstractContext) beanManager.getContext(bean.getScope());
                context.destroy(bean);
                log(INFO, "Destroyed {0}", bean.getBeanClass().getSimpleName());
            } catch (Exception x) {
                log(WARNING, "Unable to destroy {0}", x, bean);
            }
        }
        System.gc();
    }

    public static class TemporaryClean {
    }
}
