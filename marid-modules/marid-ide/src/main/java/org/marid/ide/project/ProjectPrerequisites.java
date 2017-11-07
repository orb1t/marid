/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.ide.project;

import org.apache.maven.model.*;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.marid.ide.common.IdeValues;
import org.marid.ide.settings.JavaSettings;
import org.marid.ide.settings.MavenSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Properties;

import static java.util.Collections.singletonList;
import static org.marid.misc.Builder.build;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectPrerequisites {

	private final MavenSettings mavenSettings;
	private final JavaSettings javaSettings;
	private final IdeValues ideValues;

	@Autowired
	public ProjectPrerequisites(MavenSettings mavenSettings, JavaSettings javaSettings, IdeValues ideValues) {
		this.mavenSettings = mavenSettings;
		this.javaSettings = javaSettings;
		this.ideValues = ideValues;
	}

	public void apply(ProjectProfile profile) {
		final Builder builder = new Builder(profile);
		builder.applyProperties();
		builder.applyBuild();
		builder.applyRuntimeDependency();
		builder.applyPlugins();
	}

	public static boolean is(Dependency dependency, String groupId, String artifactId) {
		return groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId());
	}

	public static boolean is(Plugin plugin, String groupId, String artifactId) {
		return groupId.equals(plugin.getGroupId()) && artifactId.equals(plugin.getArtifactId());
	}

	private class Builder {

		private final Model model;

		private Builder(ProjectProfile profile) {
			model = profile.getModel();
		}

		private void applyProperties() {
			final Properties properties = model.getProperties();
			properties.setProperty("project.build.sourceEncoding", "UTF-8");
			properties.setProperty("project.reporting.outputEncoding", "UTF-8");
			properties.setProperty("maven.compiler.source", "1.8");
			properties.setProperty("maven.compiler.target", "1.8");
			properties.setProperty("marid.version", ideValues.implementationVersion);
		}

		private void applyBuild() {
			if (model.getBuild() == null) {
				model.setBuild(new Build());
			}
		}

		private void applyPlugins() {
			applyCompilerPlugin();
			applyJarPlugin();
			applyDependencyMavenPlugin();
			applyResourcesPlugin();
			applyExecPlugin();
		}

		private void applyCompilerPlugin() {
			model.getBuild().getPlugins().removeIf(p -> "maven-compiler-plugin".equals(p.getArtifactId()));
			model.getBuild().getPlugins().add(build(new Plugin(), plugin -> {
				plugin.setArtifactId("maven-compiler-plugin");
				plugin.setVersion(mavenSettings.compilerPluginVersion.get());
				plugin.setConfiguration(build(new Xpp3Dom("configuration"), configuration -> {
					addChild(configuration, "showWarnings", "true");
					addChild(configuration, "showDeprecation", "true");
					addChild(configuration, "parameters", "true");
				}));
			}));
		}

		private void applyJarPlugin() {
			model.getBuild().getPlugins().removeIf(p -> "maven-jar-plugin".equals(p.getArtifactId()));
			final Plugin plugin = new Plugin();
			plugin.setArtifactId("maven-jar-plugin");
			plugin.setVersion(mavenSettings.jarPluginVersion.get());
			model.getBuild().getPlugins().add(plugin);
			final Xpp3Dom configuration = new Xpp3Dom("configuration");
			plugin.setConfiguration(configuration);
			final Xpp3Dom archive = new Xpp3Dom("archive");
			configuration.addChild(archive);
			final Xpp3Dom manifest = new Xpp3Dom("manifest");
			archive.addChild(manifest);
			addChild(manifest, "addClasspath", "true");
			addChild(manifest, "mainClass", "org.marid.runtime.MaridLauncher");
			addChild(manifest, "classpathPrefix", "lib");
			final Xpp3Dom manifestEntries = new Xpp3Dom("manifestEntries");
			archive.addChild(manifestEntries);
		}

		private void applyDependencyMavenPlugin() {
			final Plugin dependencyPlugin = model.getBuild().getPlugins().stream()
					.filter(p -> "maven-dependency-plugin".equals(p.getArtifactId()))
					.findAny()
					.orElseGet(() -> {
						final Plugin plugin = new Plugin();
						plugin.setArtifactId("maven-dependency-plugin");
						model.getBuild().getPlugins().add(plugin);
						return plugin;
					});
			dependencyPlugin.setVersion(mavenSettings.dependencyPluginVersion.get());
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

		private void applyResourcesPlugin() {
			final Plugin resourcesPlugin = model.getBuild().getPlugins().stream()
					.filter(p -> "maven-resources-plugin".equals(p.getArtifactId()))
					.findAny()
					.orElseGet(() -> {
						final Plugin plugin = new Plugin();
						plugin.setArtifactId("maven-resources-plugin");
						model.getBuild().getPlugins().add(plugin);
						return plugin;
					});
			resourcesPlugin.setVersion(mavenSettings.resourcesPluginVersion.get());
		}

		private void applyExecPlugin() {
			final Plugin plugin = model.getBuild().getPlugins().stream()
					.filter(p -> "exec-maven-plugin".equals(p.getArtifactId()))
					.findAny()
					.orElseGet(() -> {
						final Plugin p = new Plugin();
						p.setGroupId("org.codehaus.mojo");
						p.setArtifactId("exec-maven-plugin");
						model.getBuild().getPlugins().add(p);
						return p;
					});
			plugin.setVersion(mavenSettings.execPluginVersion.get());
			final PluginExecution execExecution = plugin.getExecutions().stream()
					.filter(e -> "default-cli".equals(e.getId()))
					.findAny()
					.orElseGet(() -> {
						final PluginExecution execution = new PluginExecution();
						plugin.getExecutions().add(execution);
						return execution;
					});
			execExecution.setGoals(Collections.singletonList("exec"));
			execExecution.setId("default-cli");

			final Xpp3Dom configuration = new Xpp3Dom("configuration");
			execExecution.setConfiguration(configuration);

			addChild(configuration, "executable", javaSettings.getJavaExecutable());
			addChild(configuration, "workingDirectory", "${project.build.directory}");

			final Xpp3Dom arguments = new Xpp3Dom("arguments");
			configuration.addChild(arguments);
			addChild(arguments, "argument", "-jar");
			addChild(arguments, "argument", "${project.build.finalName}.${project.packaging}");
		}

		private void applyRuntimeDependency() {
			final Dependency runtimeDependency = model.getDependencies().stream()
					.filter(d -> is(d, "org.marid", "marid-runtime"))
					.findFirst()
					.orElseGet(() -> {
						final Dependency dependency = new Dependency();
						dependency.setGroupId("org.marid");
						dependency.setArtifactId("marid-runtime");
						model.getDependencies().add(dependency);
						return dependency;
					});
			runtimeDependency.setVersion("${marid.version}");
		}

		private void addChild(Xpp3Dom parent, String tag, String value) {
			final Xpp3Dom node = new Xpp3Dom(tag);
			if (value != null) {
				node.setValue(value);
			}
			parent.addChild(node);
		}
	}
}
