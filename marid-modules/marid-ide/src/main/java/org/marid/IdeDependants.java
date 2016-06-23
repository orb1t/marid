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

package org.marid;

import org.marid.ide.dependants.conf.SimpleUIConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeDependants {

    public static AnnotationConfigApplicationContext startDependant(String name, Class<?> configuration) {
        return startDependant(Ide.context, name, configuration);
    }

    public static AnnotationConfigApplicationContext startDependant(ApplicationContext parent, String name, Class<?> configuration) {
        return startDependant(context -> {
            context.setDisplayName(name);
            context.register(configuration);
            context.register(SimpleUIConfig.class);
            context.setParent(parent);
        });
    }

    public static AnnotationConfigApplicationContext startDependant(Consumer<AnnotationConfigApplicationContext> contextConsumer) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        contextConsumer.accept(context);
        context.refresh();
        context.start();
        return context;
    }
}
