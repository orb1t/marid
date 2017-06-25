/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.marid.ide.project;

import org.apache.maven.cli.MaridMavenCli;
import org.apache.maven.cli.MaridMavenCliRequest;
import org.marid.maven.MavenBuildResult;
import org.marid.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
@PrototypeComponent
public class ProjectMavenBuilder {

    private final List<String> goals = new ArrayList<>();
    private final List<String> profiles = new ArrayList<>();
    private final ApplicationEventPublisher eventPublisher;

    private ProjectProfile profile;

    @Autowired
    public ProjectMavenBuilder(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public ProjectMavenBuilder goals(String... goals) {
        Collections.addAll(this.goals, goals);
        return this;
    }

    public ProjectMavenBuilder profiles(String... ids) {
        Collections.addAll(profiles, ids);
        return this;
    }

    public ProjectMavenBuilder profile(ProjectProfile profile) {
        this.profile = profile;
        return this;
    }

    public void build(Consumer<MavenBuildResult> consumer) {
        final long start = System.currentTimeMillis();
        final List<String> argList = new ArrayList<>();
        argList.add("-P" + String.join(",", profiles));
        argList.addAll(goals);
        final String[] args = argList.toArray(new String[argList.size()]);
        final List<Throwable> exceptions = new ArrayList<>();
        final MaridMavenCliRequest request = new MaridMavenCliRequest(args, null).directory(profile.getPath());
        final MaridMavenCli cli = new MaridMavenCli(null, eventPublisher, profile);
        try {
            cli.doMain(request);
        } catch (Exception x) {
            exceptions.add(x);
        }
        consumer.accept(new MavenBuildResult(System.currentTimeMillis() - start, exceptions));
    }
}
