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

import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.util.Pair;
import org.marid.jfx.LocalizedStrings;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.stream.Stream;

import static java.util.stream.Stream.of;
import static org.marid.ide.project.ProfileReflections.listeners;
import static org.marid.ide.project.ProfileReflections.observableStream;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Service
@Lazy(false)
public class ProjectGarbageCollector implements LogSupport {

    private final ProjectManager manager;

    @Autowired
    public ProjectGarbageCollector(ProjectManager manager) {
        this.manager = manager;
    }

    @Scheduled(fixedDelay = 10_000L, initialDelay = 10_000L)
    public void collect() throws Exception {
        Platform.runLater(() -> {
            for (final ProjectProfile profile : manager.getProfiles()) {
                final Stream<Observable> predefined = of(LocalizedStrings.LOCALE);
                final Stream<Observable> project = observableStream(profile).flatMap(p -> of(p.getValue()));
                final int collected = Stream.concat(predefined, project)
                        .map(o -> new Pair<>(o, listeners(o)))
                        .mapToInt(p -> ProfileReflections.collect(p.getKey(), p.getValue()))
                        .sum();
                if (collected > 0) {
                    log(INFO, "Collected {0} listeners in {1}", collected, profile);
                }
            }
        });
    }
}
