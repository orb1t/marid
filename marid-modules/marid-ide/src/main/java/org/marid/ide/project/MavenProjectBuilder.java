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

import com.google.inject.Module;
import org.apache.maven.Maven;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequestPopulator;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.extension.internal.CoreExports;
import org.apache.maven.extension.internal.CoreExtensionEntry;
import org.apache.maven.model.Profile;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.logging.BaseLoggerManager;
import org.codehaus.plexus.logging.Logger;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.transport.wagon.WagonTransporterFactory;
import org.marid.logging.LogSupport;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.LogRecord;

/**
 * @author Dmitry Ovchinnikov
 */
public class MavenProjectBuilder implements LogSupport {

    private final ProjectProfile profile;
    private final Consumer<LogRecord> logRecordConsumer;
    private final Map<String, ProjectPlexusLogger> loggerMap = new ConcurrentHashMap<>();
    private final List<String> goals = new ArrayList<>();
    private final List<Profile> profiles = new ArrayList<>();

    public MavenProjectBuilder(ProjectProfile profile, Consumer<LogRecord> logRecordConsumer) {
        this.profile = profile;
        this.logRecordConsumer = logRecordConsumer;
    }

    private ProjectPlexusLogger logger(String name) {
        return loggerMap.computeIfAbsent(name, k -> new ProjectPlexusLogger(k, logRecordConsumer));
    }

    public MavenProjectBuilder goals(String... goals) {
        Collections.addAll(this.goals, goals);
        return this;
    }

    public MavenProjectBuilder profiles(String... ids) {
        final Set<String> idSet = new HashSet<>(Arrays.asList(ids));
        for (final Profile p : profile.getModel().getProfiles()) {
            if (idSet.contains(p.getId()) && !profiles.contains(p)) {
                profiles.add(p);
            }
        }
        return this;
    }

    private PlexusContainer buildPlexusContainer() throws Exception {
        final ClassWorld classWorld = new ClassWorld("plexus.core", Thread.currentThread().getContextClassLoader());
        final ClassRealm classRealm = classWorld.getClassRealm("plexus.core");
        final CoreExtensionEntry extensionEntry = CoreExtensionEntry.discoverFrom(classRealm);
        final Module bindModule = binder -> {
            binder.bind(RepositoryConnectorFactory.class).to(BasicRepositoryConnectorFactory.class);
            binder.bind(TransporterFactory.class).to(HttpTransporterFactory.class);
            binder.bind(TransporterFactory.class).to(WagonTransporterFactory.class);
            binder.bind(TransporterFactory.class).to(FileTransporterFactory.class);
            binder.bind(CoreExports.class).toInstance(
                    new CoreExports(
                            classRealm,
                            extensionEntry.getExportedArtifacts(),
                            extensionEntry.getExportedPackages()));
        };
        final DefaultPlexusContainer container = new DefaultPlexusContainer(new DefaultContainerConfiguration()
                .setClassWorld(classWorld)
                .setRealm(classRealm)
                .setClassPathScanning(PlexusConstants.SCANNING_INDEX)
                .setAutoWiring(true)
                .setName("maven"), bindModule);
        container.setLoggerManager(new BaseLoggerManager() {
            @Override
            protected Logger createLogger(String name) {
                return logger(name);
            }
        });
        return container;
    }

    private MavenExecutionRequest mavenExecutionRequest(PlexusContainer plexusContainer) throws Exception {
        final DefaultMavenExecutionRequest request = new DefaultMavenExecutionRequest();
        final MavenExecutionRequestPopulator populator = plexusContainer.lookup(MavenExecutionRequestPopulator.class);
        populator.populateDefaults(request);
        request.setMultiModuleProjectDirectory(profile.getPath().toFile());
        return request
                .setOffline(false)
                .setGoals(goals)
                .setSystemProperties(System.getProperties())
                .setUserProperties(new Properties())
                .setReactorFailureBehavior(MavenExecutionRequest.REACTOR_FAIL_AT_END)
                .setShowErrors(true)
                .setGlobalChecksumPolicy(MavenExecutionRequest.CHECKSUM_POLICY_WARN)
                .setPom(profile.getPomFile().toFile())
                .setBaseDirectory(profile.getPath().toFile())
                .setTransferListener(new ProjectMavenTransferListener(profile))
                .setCacheNotFound(true)
                .setInteractiveMode(true)
                .setCacheTransferError(false)
                .setProfiles(profiles.isEmpty() ? null : profiles);
    }

    public Thread build(Consumer<MavenExecutionResult> consumer) {
        final Thread thread = new Thread(() -> {
            PlexusContainer plexusContainer = null;
            try {
                plexusContainer = buildPlexusContainer();
                final MavenExecutionRequest mavenExecutionRequest = mavenExecutionRequest(plexusContainer);
                final Maven maven = plexusContainer.lookup(Maven.class);
                Thread.currentThread().setContextClassLoader(plexusContainer.getContainerRealm());
                final MavenExecutionResult result = maven.execute(mavenExecutionRequest);
                consumer.accept(result);
            } catch (Exception x) {
                log(WARNING, "Unable to execute maven", x);
            } finally {
                if (plexusContainer != null) {
                    plexusContainer.dispose();
                }
            }
        });
        thread.start();
        return thread;
    }
}
