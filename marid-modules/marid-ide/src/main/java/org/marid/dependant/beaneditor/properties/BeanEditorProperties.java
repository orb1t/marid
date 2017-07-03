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

package org.marid.dependant.beaneditor.properties;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import org.marid.ide.event.TextFileAddedEvent;
import org.marid.ide.event.TextFileChangedEvent;
import org.marid.ide.event.TextFileMovedEvent;
import org.marid.ide.event.TextFileRemovedEvent;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.TreeMap;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;
import static org.springframework.core.io.support.PropertiesLoaderUtils.loadProperties;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class BeanEditorProperties {

    private final Path beansProperties;
    private final ProjectProfile profile;
    public final ObservableMap<String, String> properties = FXCollections.observableMap(new TreeMap<>());

    @Autowired
    public BeanEditorProperties(ProjectProfile profile) {
        this.profile = profile;
        this.beansProperties = profile.getSrcMainResources().resolve("beans.properties");
    }

    @PostConstruct
    private void load() {
        if (Files.isReadable(beansProperties)) {
            final EncodedResource resource = new EncodedResource(new FileSystemResource(beansProperties.toFile()));
            try {
                final Properties properties = loadProperties(resource);
                this.properties.clear();
                for (final String key : properties.stringPropertyNames()) {
                    this.properties.put(key, properties.getProperty(key));
                }
            } catch (IOException x) {
                log(WARNING, "Unable to load beans.properties", x);
            }
        }
    }

    @Bean
    public Path beansProperties() {
        return beansProperties;
    }

    @EventListener(condition = "@beansProperties.equals(#event.source)")
    public void onFileChange(TextFileChangedEvent event) throws IOException {
        Platform.runLater(this::load);
    }

    @EventListener(condition = "@beansProperties.equals(#event.source)")
    public void onFileAdded(TextFileAddedEvent event) throws IOException {
        Platform.runLater(this::load);
    }

    @EventListener(condition = "@beansProperties.equals(#event.source)")
    public void onFileMoved(TextFileMovedEvent event) throws IOException {
        Platform.runLater(properties::clear);
    }

    @EventListener(condition = "@beansProperties.equals(#event.source)")
    public void onFileRemoved(TextFileRemovedEvent event) throws IOException {
        Platform.runLater(properties::clear);
    }
}
