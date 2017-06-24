/*
 * Copyright 2017 Dmitry Ovchinnikov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.marid.ide.common;

import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class Directories {

    private final Path userHome;
    private final Path marid;
    private final Path profiles;

    public Directories() {
        userHome = Paths.get(System.getProperty("user.home"));
        marid = userHome.resolve("marid");
        profiles = marid.resolve("profiles");
    }

    @PostConstruct
    private void init() throws IOException {
        Files.createDirectories(profiles);
    }

    public Path getUserHome() {
        return userHome;
    }

    public Path getMarid() {
        return marid;
    }

    public Path getProfiles() {
        return profiles;
    }
}
