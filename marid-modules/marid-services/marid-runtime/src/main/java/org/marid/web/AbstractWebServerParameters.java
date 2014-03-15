/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.web;

import org.marid.service.AbstractMaridServiceParameters;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.singletonMap;

/**
 * @author Dmitry Ovchinnikov
 */
public class AbstractWebServerParameters extends AbstractMaridServiceParameters {

    Map<String, Path> dirMap = singletonMap("default", Paths.get(System.getProperty("user.home"), "marid", "web"));
    Map<String, Pattern> vHostPatternMap = Collections.emptyMap();
    Map<String, String> vHostMap = Collections.emptyMap();
    List<String> defaultPages = Collections.emptyList();

    public Map<String, Path> getDirMap() {
        return dirMap;
    }

    public void setDirMap(Map<String, Path> dirMap) {
        this.dirMap = dirMap;
    }

    public Map<String, Pattern> getvHostPatternMap() {
        return vHostPatternMap;
    }

    public void setvHostPatternMap(Map<String, Pattern> vHostPatternMap) {
        this.vHostPatternMap = vHostPatternMap;
    }

    public Map<String, String> getvHostMap() {
        return vHostMap;
    }

    public void setvHostMap(Map<String, String> vHostMap) {
        this.vHostMap = vHostMap;
    }

    public List<String> getDefaultPages() {
        return defaultPages;
    }

    public void setDefaultPages(List<String> defaultPages) {
        this.defaultPages = defaultPages;
    }
}
