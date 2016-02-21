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

import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Prerequisites;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.marid.ide.settings.SettingsHolder;
import org.marid.ide.settings.SettingsManager;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class ProjectPrerequisites {

    private final SettingsHolder settingsHolder;

    @Inject
    public ProjectPrerequisites(SettingsManager settingsManager) {
        settingsHolder = new SettingsHolder(settingsManager.preferences());
    }

    void apply(ProjectProfile profile) {
        applyPrerequisites(profile);
        applyProperties(profile);
        applyBuild(profile);
        applyPlugins(profile);
    }

    void applyPrerequisites(ProjectProfile profile) {
        final Prerequisites prerequisites = new Prerequisites();
        prerequisites.setMaven("3.3");
        profile.getModel().setPrerequisites(prerequisites);
    }

    void applyProperties(ProjectProfile profile) {
        profile.getModel().getProperties().setProperty("project.build.sourceEncoding", "UTF-8");
        profile.getModel().getProperties().setProperty("project.reporting.outputEncoding", "UTF-8");
    }

    void applyBuild(ProjectProfile profile) {
        if (profile.getModel().getBuild() == null) {
            profile.getModel().setBuild(new Build());
        }
    }

    void applyPlugins(ProjectProfile profile) {
        applyExecMavenPlugin(profile);
    }

    void applyExecMavenPlugin(ProjectProfile profile) {
        final Plugin execMavenPlugin = profile.getModel().getBuild().getPlugins().stream()
                .filter(p -> "org.codehaus.mojo".equals(p.getGroupId()))
                .filter(p -> "exec-maven-plugin".equals(p.getArtifactId()))
                .findAny()
                .orElseGet(() -> {
                    final Plugin plugin = new Plugin();
                    plugin.setGroupId("org.codehaus.mojo");
                    plugin.setArtifactId("exec-maven-plugin");
                    plugin.setVersion("1.4.0");
                    profile.getModel().getBuild().getPlugins().add(plugin);
                    return plugin;
                });
        final PluginExecution runInIdeExecution = execMavenPlugin.getExecutions().stream()
                .filter(e -> "run-in-ide".equals(e.getId()))
                .findAny()
                .orElseGet(() -> {
                    final PluginExecution execution = new PluginExecution();
                    execution.setId("run-in-ide");
                    execMavenPlugin.getExecutions().add(execution);
                    return execution;
                });
        runInIdeExecution.setPhase(null);
        runInIdeExecution.setGoals(new ArrayList<>(Collections.singletonList("exec")));
        final Xpp3Dom configuration = new Xpp3Dom("configuration");
        runInIdeExecution.setConfiguration(configuration);
        {
            final Xpp3Dom executable = new Xpp3Dom("executable");
            executable.setValue(settingsHolder.javaExecutable.getValue());
            configuration.addChild(executable);
        }
        {
            final Xpp3Dom longClasspath = new Xpp3Dom("longClasspath");
            longClasspath.setValue("true");
            configuration.addChild(longClasspath);
        }
        {
            final Xpp3Dom workingDirectory = new Xpp3Dom("workingDirectory");
            workingDirectory.setValue(profile.getPath().toString());
            configuration.addChild(workingDirectory);
        }
        {
            final Xpp3Dom arguments = new Xpp3Dom("arguments");
            final Consumer<String> argGenerator = v -> {
                final Xpp3Dom argument = new Xpp3Dom("argument");
                argument.setValue(v);
                arguments.addChild(argument);
            };
            for (final String arg : settingsHolder.javaArguments.getValue()) {
                argGenerator.accept(arg);
            }
            argGenerator.accept("-jar");
            argGenerator.accept("${project.build.finalName}");
            argGenerator.accept("${project.run.args}");
            configuration.addChild(arguments);
        }
    }
}
