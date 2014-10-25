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

import java.lang.annotation.*;

/**
 * @author Dmitry Ovchinnikov
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface WebServerParameters {

    Dir[] dirs() default {@Dir()};

    VHost[] vHosts() default {};

    String[] defaultPages() default {"index.html"};

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Dir {

        String name() default "default";

        String dir() default "${user.home}/marid/web";
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface VHost {

        String name();

        String pattern();
    }
}
