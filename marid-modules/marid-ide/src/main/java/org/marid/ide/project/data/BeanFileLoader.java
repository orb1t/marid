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

package org.marid.ide.project.data;

import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.spring.xml.data.BeanFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.inject.Provider;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileLoader {

    private final Provider<ProjectManager> projectManager;

    @Autowired
    public BeanFileLoader(Provider<ProjectManager> projectManager) {
        this.projectManager = projectManager;
    }

    public BeanFile load(Path path) throws IOException, SAXException, ParserConfigurationException {
        try (final InputStream inputStream = Files.newInputStream(path)) {
            return load(inputStream);
        }
    }

    public BeanFile load(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setCoalescing(true);
        documentBuilderFactory.setNamespaceAware(true);
        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        final Document document = documentBuilder.parse(inputStream);
        return new Loader(document).load();
    }

    private class Loader {

        private final ProjectProfile profile;
        private final URLClassLoader classLoader;
        private final Document document;

        private Loader(Document document) {
            this.document = document;
            this.profile = projectManager.get().getProfile();
            this.classLoader = profile.classLoader();
        }

        private BeanFile load() {
            final BeanFile beanFile = new BeanFile();
            return beanFile;
        }
    }
}
