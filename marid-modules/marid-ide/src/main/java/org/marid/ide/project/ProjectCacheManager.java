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

package org.marid.ide.project;

import org.marid.logging.LogSupport;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLClassLoader;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectCacheManager implements LogSupport {

    private final ObjectProvider<ProjectMavenBuilder> projectBuilder;

    @Autowired
    public ProjectCacheManager(ObjectProvider<ProjectMavenBuilder> projectBuilder) {
        this.projectBuilder = projectBuilder;
    }

    public static URLClassLoader getClassLoader(ProjectProfile profile) {
        return profile.cacheEntry.getClassLoader();
    }

    public void build(ProjectProfile profile) {
        projectBuilder.getObject().build(profile, result -> {
            try {
                log(INFO, "[{0}] Built {1}", profile, result);
                profile.cacheEntry.update();
                log(INFO, "[{0}] Updated", profile);
            } catch (Exception x) {
                log(WARNING, "Unable to update cache {0}", x, profile);
            }
        }, profile.logger()::log);
    }

    public static boolean containsBean(ProjectProfile profile, String name) {
        for (final BeanFile file : profile.beanFiles.values()) {
            if (file.allBeans().anyMatch(b -> b.nameProperty().isEqualTo(name).get())) {
                return true;
            }
        }
        return false;
    }

    public static String generateBeanName(ProjectProfile profile, String name) {
        while (containsBean(profile, name)) {
            name += "_new";
        }
        return name;
    }
}
