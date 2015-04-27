/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid

import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask
import java.util.regex.Pattern

/**
 * @author Dmitry Ovchinnikov
 */

new URL("http://kkre-1.narod.ru/pevc.htm").withReader {ar ->
    def atext = ar.text;
    def pattern = Pattern.compile("href=\"(.+)\"");
    def matcher = pattern.matcher(atext);
    List<ForkJoinTask<?>> taskList = new ArrayList<>();
    while (matcher.find()) {
        def url = matcher.group(1);
        taskList.add(ForkJoinPool.commonPool().submit({
            try {
                new URL(url).withReader { r ->
                    def text = r.text.toLowerCase();
                    if (text.contains("клен") || text.contains("клён")) {
                        println(url);
                    }
                }
            } catch (e) {
                println(e);
            }
        }));
    }
    taskList.each {it.join()};
}