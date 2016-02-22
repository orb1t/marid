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

import org.apache.maven.model.*;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.marid.ide.settings.JavaSettings;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov
 */
@Dependent
public class ProjectPrerequisites {

    private final JavaSettings javaSettings;

    @Inject
    public ProjectPrerequisites(JavaSettings javaSettings) {
        this.javaSettings = javaSettings;
    }

    void apply(ProjectProfile profile) {
        applyPrerequisites(profile);
        applyProperties(profile);
        applyBuild(profile);
        applyPluginManagement(profile);
        applyPlugins(profile);
        applyRuntimeDependency(profile);
    }

    void applyPrerequisites(ProjectProfile profile) {
        final Prerequisites prerequisites = new Prerequisites();
        prerequisites.setMaven("3.3");
        profile.getModel().setPrerequisites(prerequisites);
    }

    void applyProperties(ProjectProfile profile) {
        final Properties properties = profile.getModel().getProperties();
        properties.setProperty("project.build.sourceEncoding", "UTF-8");
        properties.setProperty("project.reporting.outputEncoding", "UTF-8");
        if (!properties.containsKey("marid.runtime.version")) {
            properties.setProperty("marid.runtime.version", System.getProperty("implementation.version"));
        }
    }

    void applyBuild(ProjectProfile profile) {
        if (profile.getModel().getBuild() == null) {
            profile.getModel().setBuild(new Build());
        }
    }

    void applyPluginManagement(ProjectProfile profile) {
        if (profile.getModel().getBuild().getPluginManagement() == null) {
            profile.getModel().getBuild().setPluginManagement(new PluginManagement());
        }
        applyExecMavenPluginManagement(profile);
    }

    void applyExecMavenPluginManagement(ProjectProfile profile) {
        final PluginManagement pluginManagement = profile.getModel().getBuild().getPluginManagement();
        pluginManagement.getPlugins().removeIf(p ->
                "org.codehaus.mojo".equals(p.getGroupId()) && "exec-maven-plugin".equals(p.getArtifactId()));
        final Plugin plugin = new Plugin();
        plugin.setGroupId("org.codehaus.mojo");
        plugin.setArtifactId("exec-maven-plugin");
        plugin.setVersion("1.4.0");
        pluginManagement.addPlugin(plugin);
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
        addChild(configuration, "executable", javaSettings.getJavaExecutable());
        addChild(configuration, "longClasspath", "true");
        addChild(configuration, "workingDirectory", "${project.build.directory}");
        addChild(configuration, "outputFile", "${project.build.directory}/output.log");
        final Xpp3Dom arguments = new Xpp3Dom("arguments");
        for (final String arg : javaSettings.getJavaArguments()) {
            addChild(arguments, "argument", arg);
        }
        addChild(arguments, "argument", "-cp");
        addChild(arguments, "classpath", null);
        addChild(arguments, "argument", "org.marid.runtime.MaridLauncher");
        configuration.addChild(arguments);
    }

    void applyRuntimeDependency(ProjectProfile profile) {
        final List<Dependency> dependencies = profile.getModel().getDependencies();
        dependencies.removeIf(d -> "org.marid".equals(d.getGroupId()) && "marid-runtime".equals(d.getArtifactId()));
        final Dependency dependency = new Dependency();
        dependency.setGroupId("org.marid");
        dependency.setArtifactId("marid-runtime");
        dependency.setVersion("${marid.runtime.version}");
        dependencies.add(dependency);
    }

    private void addChild(Xpp3Dom parent, String tag, String value) {
        final Xpp3Dom node = new Xpp3Dom(tag);
        if (value != null) {
            node.setValue(value);
        }
        parent.addChild(node);
    }
}
