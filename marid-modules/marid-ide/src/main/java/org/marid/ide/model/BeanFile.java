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

package org.marid.ide.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.ide.project.ProjectProfile;
import org.marid.io.Xmls;
import org.marid.runtime.beans.Bean;
import org.marid.runtime.context.MaridConfiguration;
import org.w3c.dom.Element;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.marid.ide.project.ProjectFileType.BEANS_XML;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanFile {

    public final ObservableList<BeanData> beans = FXCollections.observableArrayList(BeanData::observables);

    public void save(ProjectProfile profile) {
        final MaridConfiguration context = new MaridConfiguration(beans.stream().map(BeanData::toBean).toArray(Bean[]::new));
        Xmls.writeFormatted(d -> {
            final Element root = d.createElement("beans");
            d.appendChild(root);
            context.writeTo(root);
        }, profile.get(BEANS_XML));
    }

    public void load(ProjectProfile profile) {
        final Path file = profile.get(BEANS_XML);
        if (Files.notExists(file)) {
            save(profile);
            return;
        }
        final AtomicReference<Element> elementRef = new AtomicReference<>();
        Xmls.read(d -> elementRef.set(d.getDocumentElement()), file);
        final MaridConfiguration context = new MaridConfiguration(elementRef.get());
        beans.setAll(Stream.of(context.beans).map(BeanData::new).toArray(BeanData[]::new));
    }

    public Bean[] toBeans() {
        return beans.stream().map(BeanData::toBean).toArray(Bean[]::new);
    }
}
