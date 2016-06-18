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

import javafx.scene.control.Dialog;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeDependants {

    private static final LinkedList<AnnotationConfigApplicationContext> DEPENDENT_CONTEXTS = new LinkedList<>();

    public static <T extends Window> T newWindow(Class<T> type) {
        final AnnotationConfigApplicationContext context = child(type);
        final T window = context.getBean(type);
        window.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> closeContext(false, context));
        return window;
    }

    public static <T extends Dialog<?>> T newDialog(Class<T> type) {
        final AnnotationConfigApplicationContext context = child(type);
        final T dialog = context.getBean(type);
        dialog.showingProperty().addListener((observable, oldValue, newValue) -> closeContext(newValue, context));
        return dialog;
    }

    public static <T> List<T> getDependants(Class<T> type) {
        final List<T> list = new ArrayList<>();
        for (final AnnotationConfigApplicationContext context : DEPENDENT_CONTEXTS) {
            list.addAll(context.getBeansOfType(type).values());
        }
        return list;
    }

    static AnnotationConfigApplicationContext child(Class<?> type) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.setDisplayName(type.getName());
        context.setParent(Ide.context);
        context.scan(type.getPackage().getName());
        context.refresh();
        context.start();
        DEPENDENT_CONTEXTS.add(context);
        return context;
    }

    static void closeContext(boolean showing, AnnotationConfigApplicationContext context) {
        if (!showing) {
            try {
                context.close();
            } finally {
                DEPENDENT_CONTEXTS.remove(context);
            }
        }
    }
}
