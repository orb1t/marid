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

package org.marid.util;

import org.apache.maven.model.Dependency;

import java.util.Objects;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
public interface Dependencies {

    static boolean equals(Dependency d1, Dependency d2) {
        if (!Objects.equals(d1.getGroupId(), d2.getGroupId())) {
            return false;
        }
        if (!Objects.equals(d1.getArtifactId(), d2.getArtifactId())) {
            return false;
        }
        if (!Objects.equals(d1.getVersion(), d2.getVersion())) {
            return false;
        }
        return true;
    }
}
