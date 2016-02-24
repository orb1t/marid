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

import org.marid.logging.LogSupport;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.*;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanLoggingExtension implements Extension, LogSupport {

    public void observeBeanCreation(@Observes ProcessBean<?> processBean) {
        log(FINE, "Bean {0} processed", processBean.getBean());
    }

    public void observerBeforeShutdown(@Observes BeforeShutdown beforeShutdown) {
        log(INFO, "Shutting down");
    }

    public void observerAfterTypeDiscovery(@Observes AfterTypeDiscovery afterTypeDiscovery) {
        log(INFO, "Type discovery: {0} alternatives, {1} decorators, {2} interceptors",
                afterTypeDiscovery.getAlternatives().size(),
                afterTypeDiscovery.getDecorators().size(),
                afterTypeDiscovery.getInterceptors().size());
    }

    public void observerDeploymentValidation(@Observes AfterDeploymentValidation afterDeploymentValidation) {
        log(INFO, "Deployment validation completed successfully");
    }

    public void observerAfterBeanDiscovery(@Observes AfterBeanDiscovery afterBeanDiscovery) {
        log(INFO, "Bean discovery completed successfully");
    }

    public void observerBeforeBeanDiscovery(@Observes BeforeBeanDiscovery afterBeanDiscovery) {
        log(INFO, "Bean discovery started");
    }
}
