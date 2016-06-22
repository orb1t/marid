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

package org.marid.ide.project.event;

import org.marid.ide.project.ProjectProfile;
import org.springframework.context.ApplicationEvent;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanNameChangedEvent extends ApplicationEvent {

    private final String oldBeanName;
    private final String newBeanName;

    public BeanNameChangedEvent(ProjectProfile source, String oldBeanName, String newBeanName) {
        super(source);
        this.oldBeanName = oldBeanName;
        this.newBeanName = newBeanName;
    }

    @Override
    public ProjectProfile getSource() {
        return (ProjectProfile) super.getSource();
    }

    public String getOldBeanName() {
        return oldBeanName;
    }

    public String getNewBeanName() {
        return newBeanName;
    }
}
