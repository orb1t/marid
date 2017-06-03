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

package org.marid.ide.settings;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class JavaSettings extends AbstractSettings {

    public final Path java = Paths.get(
            System.getProperty("java.home"),
            "bin",
            "java" + (System.getProperty("os.name").contains("indows") ? ".exe" : "")
    );

    public String getJavaExecutable() {
        return preferences.get("javaExecutable", java.toString());
    }

    public void setJavaExecutable(String value) {
        if (StringUtils.isBlank(value)) {
            preferences.remove("javaExecutable");
        } else {
            preferences.put("javaExecutable", value);
        }
    }

    public String[] getJavaArguments() {
        return Optional.ofNullable(preferences.get("javaArguments", null)).map(s -> s.split("\\000")).orElse(new String[0]);
    }

    public void setJavaArguments(String[] arguments) {
        preferences.put("javaArguments", String.join("\000", arguments));
    }

    @Override
    public String getName() {
        return "Java";
    }
}
