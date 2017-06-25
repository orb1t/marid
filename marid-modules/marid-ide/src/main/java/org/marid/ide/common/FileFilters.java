/*
 *
 */

package org.marid.ide.common;

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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Repository;

import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class FileFilters {

    @Bean({"text/x-java"})
    @Qualifier("java")
    public PathMatcher javaPathMatcher() {
        return p -> FileSystems.getDefault().getPathMatcher("glob:*.java").matches(p.getFileName());
    }

    @Bean({"application/java-archive"})
    @Qualifier("jar")
    public PathMatcher jarPathMatcher() {
        return p -> FileSystems.getDefault().getPathMatcher("glob:*.jar").matches(p.getFileName());
    }

    @Bean({"text/x-java-properties"})
    @Qualifier("properties")
    public PathMatcher propertiesPathMatcher() {
        return p -> FileSystems.getDefault().getPathMatcher("glob:*.properties").matches(p.getFileName());
    }

    @Bean({"text/plain"})
    @Qualifier("lst")
    public PathMatcher confListPathMatcher() {
        return p -> FileSystems.getDefault().getPathMatcher("glob:*.lst").matches(p.getFileName());
    }

    @Bean
    @Qualifier("text-files")
    public PathMatcher textFilePathMatcher(Map<String, PathMatcher> pathMatcherMap) {
        return pathMatcherMap.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith("text/"))
                .map(Entry::getValue)
                .reduce((p1, p2) -> p -> p1.matches(p) || p2.matches(p))
                .orElseThrow(() -> new IllegalStateException("No text type filters"));
    }
}
