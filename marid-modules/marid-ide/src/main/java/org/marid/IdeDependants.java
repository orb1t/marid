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
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeDependants {

    private static final LinkedList<AnnotationConfigApplicationContext> DEPENDENT_CONTEXTS = new LinkedList<>();

    public static <T> List<T> getDependants(Class<T> type) {
        final List<T> list = new ArrayList<>();
        for (final AnnotationConfigApplicationContext context : DEPENDENT_CONTEXTS) {
            list.addAll(context.getBeansOfType(type).values());
        }
        return list;
    }

    public static AnnotationConfigApplicationContext startDependant(String name, Class<?>... classes) {
        return startDependant(context -> {
            context.setDisplayName(name);
            context.register(classes);
            context.register(SimpleUIConfig.class);
        });
    }

    public static AnnotationConfigApplicationContext startDependant(String name, Package... packages) {
        return startDependant(context -> {
            context.setDisplayName(name);
            context.scan(Stream.of(packages).map(Package::getName).toArray(String[]::new));
            context.register(SimpleUIConfig.class);
        });
    }

    public static AnnotationConfigApplicationContext startDependant(Consumer<AnnotationConfigApplicationContext> contextConsumer) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setParent(Ide.context);
        contextConsumer.accept(context);
        context.refresh();
        context.start();
        DEPENDENT_CONTEXTS.add(context);
        return context;
    }
}
