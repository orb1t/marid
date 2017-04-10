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

package org.marid.maven;

import org.apache.maven.cli.MaridMavenCliRequest;
import org.apache.maven.cli.MavenCli;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class MavenProjectBuilder implements ProjectBuilder {

    private final Path projectPath;
    private final List<String> goals = new ArrayList<>();
    private final List<String> profiles = new ArrayList<>();

    public MavenProjectBuilder(Path projectPath) {
        this.projectPath = projectPath;
    }

    @Override
    public MavenProjectBuilder goals(String... goals) {
        Collections.addAll(this.goals, goals);
        return this;
    }

    @Override
    public MavenProjectBuilder profiles(String... ids) {
        Collections.addAll(profiles, ids);
        return this;
    }

    @Override
    public void build(Consumer<MavenBuildResult> consumer) {
        final long start = System.currentTimeMillis();
        final List<String> argList = new ArrayList<>();
        argList.add("-P" + String.join(",", profiles));
        argList.addAll(goals);
        final String[] args = argList.toArray(new String[argList.size()]);
        final List<Throwable> exceptions = new ArrayList<>();
        final MaridMavenCliRequest request = new MaridMavenCliRequest(args, null)
                .directory(projectPath);
        final MavenCli cli = new MavenCli(null);
        try {
            cli.doMain(request);
        } catch (Exception x) {
            exceptions.add(x);
        }
        consumer.accept(new MavenBuildResult(System.currentTimeMillis() - start, exceptions));
    }
}
