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

import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.stage.Window;
import org.marid.ide.tabs.IdeTab;
import org.marid.spring.dependant.DependantConfiguration;
import org.marid.spring.postprocessors.MaridCommonPostProcessor;
import org.marid.spring.postprocessors.WindowAndDialogPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("dependants")
public class IdeDependants {

    private static final List<WeakReference<GenericApplicationContext>> CONTEXTS = new CopyOnWriteArrayList<>();

    private final GenericApplicationContext parent;

    @Autowired
    public IdeDependants(GenericApplicationContext parent) {
        this.parent = parent;
    }

    public GenericApplicationContext start(Consumer<AnnotationConfigApplicationContext> consumer) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext() {
            @Override
            protected void finalize() throws Throwable {
                Platform.runLater(this::close);
            }
        };
        context.getBeanFactory().addBeanPostProcessor(new MaridCommonPostProcessor());
        context.getBeanFactory().addBeanPostProcessor(new WindowAndDialogPostProcessor(context));
        context.getBeanFactory().setParentBeanFactory(parent.getDefaultListableBeanFactory());
        context.setAllowBeanDefinitionOverriding(false);
        context.setAllowCircularReferences(false);
        context.register(IdeDependants.class);
        final WeakReference<GenericApplicationContext> contextRef = new WeakReference<>(context);
        context.addApplicationListener(new ApplicationListener<ApplicationEvent>() {
            @Override
            public void onApplicationEvent(ApplicationEvent event) {
                System.gc();
                if (event instanceof ContextClosedEvent) {
                    CONTEXTS.removeIf(c -> c.get() == null || c.get() == contextRef.get());
                    final ApplicationContext ctx = ((ContextClosedEvent) event).getApplicationContext();
                    ((GenericApplicationContext) ctx).getApplicationListeners().remove(this);
                } else if (event instanceof ContextStartedEvent) {
                    CONTEXTS.removeIf(c -> c.get() == null);
                    CONTEXTS.add(contextRef);
                } else {
                    CONTEXTS.removeIf(c -> c.get() == null);
                }
            }
        });
        final AtomicReference<ApplicationListener<ContextClosedEvent>> closedRef = new AtomicReference<>();
        closedRef.set(event -> {
            context.close();
            parent.getApplicationListeners().remove(closedRef.getAndSet(null));
        });
        parent.addApplicationListener(closedRef.get());
        consumer.accept(context);
        context.refresh();
        context.start();
        return context;
    }

    public GenericApplicationContext start(Class<?> conf, Consumer<AnnotationConfigApplicationContext> consumer) {
        return start(context -> {
            context.register(conf);
            consumer.accept(context);
        });
    }

    public <T> GenericApplicationContext start(Class<? extends DependantConfiguration<T>> conf, T param, Consumer<AnnotationConfigApplicationContext> consumer) {
        return getMatched(param).findAny().map(IdeDependants::activate)
                .orElseGet(() -> start(context -> {
                    context.getBeanFactory().registerSingleton("params", param);
                    context.register(conf);
                    consumer.accept(context);
                }));
    }

    private static Stream<GenericApplicationContext> getMatched(Object param) {
        return CONTEXTS.stream().map(Reference::get).filter(Objects::nonNull).filter(c -> {
            if (c.containsBean("params")) {
                final Object params = c.getBean("params");
                return params.equals(param);
            } else {
                return false;
            }
        });
    }

    private static GenericApplicationContext activate(GenericApplicationContext context) {
        context.getBeansOfType(IdeTab.class, false, false).forEach((name, tab) -> {
            final SelectionModel<Tab> selectionModel = tab.getTabPane().getSelectionModel();
            selectionModel.select(tab);
        });
        context.getBeansOfType(Window.class, false, false).forEach((name, win) -> win.requestFocus());
        context.getBeansOfType(Dialog.class, false, false).forEach((name, win) -> win.show());
        return context;
    }

    @Override
    public String toString() {
        return parent.toString();
    }
}
