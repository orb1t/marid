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

package org.marid.ide.project.cache;

import javafx.collections.ListChangeListener;
import org.marid.ide.project.ProjectMavenBuilder;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectCacheManager implements LogSupport {

    private final ProjectManager projectManager;
    private final ProjectMavenBuilder projectBuilder;
    private final Map<ProjectProfile, ProjectCacheEntry> cache = new ConcurrentHashMap<>();

    @Autowired
    public ProjectCacheManager(ProjectManager projectManager, ProjectMavenBuilder projectBuilder) {
        this.projectManager = projectManager;
        this.projectBuilder = projectBuilder;
        final ListChangeListener<ProjectProfile> projectProfileListChangeListener = change -> {
            if (change.wasRemoved() || change.wasReplaced()) {
                for (final ProjectProfile profile : change.getRemoved()) {
                    try (final ProjectCacheEntry projectCacheEntry = cache.remove(profile)) {
                        if (projectCacheEntry != null) {
                            log(INFO, "Closing cache entry {0}", profile);
                        }
                    } catch (Exception x) {
                        log(WARNING, "Unable to close cache entry {0}", profile);
                    }
                }
            }
        };
        projectManager.getProfiles().addListener(projectProfileListChangeListener);
    }

    public Class<?> getClass(ProjectProfile profile, String type) {
        return cache.computeIfAbsent(profile, ProjectCacheEntry::new).getClass(type);
    }

    public void build(ProjectProfile profile) {
        final AtomicBoolean first = new AtomicBoolean();
        final ProjectCacheEntry entry = cache.computeIfAbsent(profile, p -> {
            first.set(true);
            return new ProjectCacheEntry(p);
        });
        if (entry.shouldBeUpdated() || first.get()) {
            projectBuilder.build(profile, result -> {
                try {
                    log(INFO, "[{0}] Built {1}", profile, result);
                    entry.update();
                    log(INFO, "[{0}] Updated", profile);
                } catch (Exception x) {
                    log(WARNING, "Unable to update cache {0}", x, profile);
                }
            }, profile.logger()::log);
        } else {
            log(INFO, "Nothing to do");
        }
    }
}
