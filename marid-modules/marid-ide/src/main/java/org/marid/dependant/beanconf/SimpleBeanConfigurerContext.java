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

package org.marid.dependant.beanconf;

import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.BeanData;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLClassLoader;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class SimpleBeanConfigurerContext {

    private final ProjectProfile profile;
    private final BeanFile beanFile;
    private final BeanData beanData;
    private final URLClassLoader classLoader;

    @Autowired
    public SimpleBeanConfigurerContext(ProjectProfile profile, BeanFile beanFile, BeanData beanData) {
        this.profile = profile;
        this.beanFile = beanFile;
        this.beanData = beanData;
        this.classLoader = profile.classLoader();
    }
}
