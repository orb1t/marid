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
import org.marid.ide.common.IdeValues;
import org.marid.ide.settings.MavenSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

import static java.util.Collections.singletonList;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectPrerequisites {

    private final MavenSettings mavenSettings;
    private final IdeValues ideValues;

    private Model model;

    @Autowired
    public ProjectPrerequisites(MavenSettings mavenSettings, IdeValues ideValues) {
        this.mavenSettings = mavenSettings;
        this.ideValues = ideValues;
    }

    public void apply(ProjectProfile profile) {
        model = profile.getModel();
        applyPrerequisites();
        applyProperties();
        applyBuild();
        applyPluginManagement();
        applyRuntimeDependency();
        applyPlugins();
    }

    void applyPrerequisites() {
        final Prerequisites prerequisites = new Prerequisites();
        prerequisites.setMaven("3.3");
        model.setPrerequisites(prerequisites);
    }

    void applyProperties() {
        final Properties properties = model.getProperties();
        properties.setProperty("project.build.sourceEncoding", "UTF-8");
        properties.setProperty("project.reporting.outputEncoding", "UTF-8");
        properties.setProperty("maven.compiler.source", "1.8");
        properties.setProperty("maven.compiler.target", "1.8");
        properties.setProperty("marid.runtime.version", ideValues.implementationVersion);
    }

    void applyBuild() {
        if (model.getBuild() == null) {
            model.setBuild(new Build());
        }
    }

    void applyPluginManagement() {
        if (model.getBuild().getPluginManagement() == null) {
            model.getBuild().setPluginManagement(new PluginManagement());
        }
        applyExecMavenPluginManagement();
    }

    void applyExecMavenPluginManagement() {
        final PluginManagement pluginManagement = model.getBuild().getPluginManagement();
        pluginManagement.getPlugins().removeIf(p -> is(p, "org.codehaus.mojo", "exec-maven-plugin"));
        final Plugin plugin = new Plugin();
        plugin.setGroupId("org.codehaus.mojo");
        plugin.setArtifactId("exec-maven-plugin");
        plugin.setVersion("1.4.0");
        pluginManagement.addPlugin(plugin);
    }

    void applyPlugins() {
        applyCompilerPlugin();
        applyJarPlugin();
        applyDependencyMavenPlugin();
        applyResourcesPluginVersion();
    }

    void applyCompilerPlugin() {
        model.getBuild().getPlugins().removeIf(p -> "maven-compiler-plugin".equals(p.getArtifactId()));
        final Plugin plugin = new Plugin();
        model.getBuild().getPlugins().add(plugin);
        plugin.setArtifactId("maven-compiler-plugin");
        plugin.setVersion(mavenSettings.getCompilerPluginVersion());
        final Dependency dependency = new Dependency();
        dependency.setGroupId("org.codehaus.plexus");
        dependency.setArtifactId("plexus-compiler-eclipse");
        dependency.setVersion(mavenSettings.getEclipseCompilerVersion());
        plugin.getDependencies().add(dependency);
        final Xpp3Dom configuration = new Xpp3Dom("configuration");
        plugin.setConfiguration(configuration);
        addChild(configuration, "compilerId", "eclipse");
    }

    void applyJarPlugin() {
        model.getBuild().getPlugins().removeIf(p -> "maven-jar-plugin".equals(p.getArtifactId()));
        final Plugin plugin = new Plugin();
        plugin.setArtifactId("maven-jar-plugin");
        plugin.setVersion(mavenSettings.getJarPluginVersion());
        model.getBuild().getPlugins().add(plugin);
        final Xpp3Dom configuration = new Xpp3Dom("configuration");
        plugin.setConfiguration(configuration);
        final Xpp3Dom archive = new Xpp3Dom("archive");
        configuration.addChild(archive);
        final Xpp3Dom manifest = new Xpp3Dom("manifest");
        archive.addChild(manifest);
        addChild(manifest, "addClasspath", "true");
        if (model.getDependencies().stream().anyMatch(d -> is(d, "org.marid", "marid-hmi"))) {
            addChild(manifest, "mainClass", "org.marid.hmi.HmiLauncher");
        } else {
            addChild(manifest, "mainClass", "org.marid.runtime.MaridLauncher");
        }
        addChild(manifest, "classpathPrefix", "lib");
        final Xpp3Dom manifestEntries = new Xpp3Dom("manifestEntries");
        archive.addChild(manifestEntries);
    }

    void applyDependencyMavenPlugin() {
        final Plugin dependencyPlugin = model.getBuild().getPlugins().stream()
                .filter(p -> "maven-dependency-plugin".equals(p.getArtifactId()))
                .findAny()
                .orElseGet(() -> {
                    final Plugin plugin = new Plugin();
                    plugin.setArtifactId("maven-dependency-plugin");
                    model.getBuild().getPlugins().add(plugin);
                    return plugin;
                });
        dependencyPlugin.setVersion(mavenSettings.getDependencyPluginVersion());
        final PluginExecution copyDependenciesExecution = dependencyPlugin.getExecutions().stream()
                .filter(e -> "copy-deps".equals(e.getId()))
                .findAny()
                .orElseGet(() -> {
                    final PluginExecution execution = new PluginExecution();
                    execution.setId("copy-deps");
                    dependencyPlugin.getExecutions().add(execution);
                    return execution;
                });
        copyDependenciesExecution.setPhase("package");
        copyDependenciesExecution.setGoals(singletonList("copy-dependencies"));
        final Xpp3Dom configuration = new Xpp3Dom("configuration");
        copyDependenciesExecution.setConfiguration(configuration);
        addChild(configuration, "outputDirectory", "${project.build.directory}/lib");
        addChild(configuration, "overWriteReleases", "true");
        addChild(configuration, "overWriteSnapshots", "true");
    }

    void applyResourcesPluginVersion() {
        final Plugin resourcesPlugin = model.getBuild().getPlugins().stream()
                .filter(p -> "maven-resources-plugin".equals(p.getArtifactId()))
                .findAny()
                .orElseGet(() -> {
                    final Plugin plugin = new Plugin();
                    plugin.setArtifactId("maven-resources-plugin");
                    model.getBuild().getPlugins().add(plugin);
                    return plugin;
                });
        resourcesPlugin.setVersion(mavenSettings.getResourcesPluginVersion());
    }

    void applyRuntimeDependency() {
        final List<Dependency> dependencies = model.getDependencies();
        final Dependency runtimeDependency = dependencies.stream()
                .filter(d -> is(d, "org.marid", "marid-runtime"))
                .findFirst()
                .orElse(null);
        final Dependency hmiDependency = dependencies.stream()
                .filter(d -> is(d, "org.marid", "marid-hmi"))
                .findFirst()
                .orElse(null);
        final Dependency dependency;
        if (runtimeDependency != null && hmiDependency != null) {
            dependencies.remove(runtimeDependency);
            dependency = hmiDependency;
        } else if (runtimeDependency == null && hmiDependency == null) {
            dependencies.add(dependency = new Dependency());
            dependency.setGroupId("org.marid");
            dependency.setArtifactId("marid-runtime");
        } else if (runtimeDependency == null) {
            dependency = hmiDependency;
        } else {
            dependency = runtimeDependency;
        }
        dependency.setVersion("${marid.runtime.version}");
    }

    private void addChild(Xpp3Dom parent, String tag, String value) {
        final Xpp3Dom node = new Xpp3Dom(tag);
        if (value != null) {
            node.setValue(value);
        }
        parent.addChild(node);
    }

    public static boolean is(Dependency dependency, String groupId, String artifactId) {
        return groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId());
    }

    public static boolean is(Plugin plugin, String groupId, String artifactId) {
        return groupId.equals(plugin.getGroupId()) && artifactId.equals(plugin.getArtifactId());
    }
}
