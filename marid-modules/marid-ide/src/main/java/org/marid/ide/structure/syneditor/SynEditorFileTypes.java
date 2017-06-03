/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.ide.structure.syneditor;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import java.nio.file.PathMatcher;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class SynEditorFileTypes {

    private final Map<String, String> extToMode = ImmutableMap.<String, String>builder()
            .put("java", "ace/mode/java")
            .put("properties", "ace/mode/properties")
            .build();

    public String getMode(String extension) {
        return extToMode.getOrDefault(extension, "ace/mode/plain_text");
    }

    @Bean
    @Qualifier("syn")
    public PathMatcher synEditorMatcher() {
        return path -> {
            final String extension = FilenameUtils.getExtension(path.getFileName().toString());
            return extToMode.containsKey(extension);
        };
    }
}
