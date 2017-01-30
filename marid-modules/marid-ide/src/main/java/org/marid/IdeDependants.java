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

import com.google.common.collect.ImmutableMap;
import javafx.scene.control.Dialog;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Tab;
import javafx.stage.Window;
import org.marid.ide.tabs.IdeTab;
import org.marid.spring.dependant.DependantConfiguration;
import org.marid.spring.postprocessors.LogBeansPostProcessor;
import org.marid.spring.postprocessors.OrderedInitPostProcessor;
import org.marid.spring.postprocessors.WindowAndDialogPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.marid.spring.dependant.DependantConfiguration.PARAM;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("dependants")
public class IdeDependants {

    private static final List<AnnotationConfigApplicationContext> CONTEXTS = new CopyOnWriteArrayList<>();

    private final GenericApplicationContext parent;

    @Autowired
    public IdeDependants(GenericApplicationContext parent) {
        this.parent = parent;
    }

    public boolean start(Consumer<AnnotationConfigApplicationContext> consumer) {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.addBeanPostProcessor(new OrderedInitPostProcessor(context));
            beanFactory.addBeanPostProcessor(new LogBeansPostProcessor());
            beanFactory.addBeanPostProcessor(new WindowAndDialogPostProcessor(context));
            beanFactory.setParentBeanFactory(parent.getDefaultListableBeanFactory());
        });
        context.setAllowBeanDefinitionOverriding(false);
        context.setAllowCircularReferences(false);
        context.register(IdeDependants.class);
        final ApplicationListener<ContextClosedEvent> closedListener = event -> CONTEXTS.remove(context);
        final ApplicationListener<ContextStartedEvent> startedListener = event -> CONTEXTS.add(context);
        context.addApplicationListener(closedListener);
        context.addApplicationListener(startedListener);
        final AtomicReference<ApplicationListener<ContextClosedEvent>> closedRef = new AtomicReference<>();
        closedRef.set(event -> {
            context.close();
            parent.getApplicationListeners().remove(closedRef.get());
        });
        parent.addApplicationListener(closedRef.get());
        consumer.accept(context);
        context.refresh();
        context.start();
        return true;
    }

    public final boolean start(Class<?> conf, Consumer<AnnotationConfigApplicationContext> consumer) {
        return start(context -> {
            context.register(conf);
            consumer.accept(context);
        });
    }

    public <T> boolean start(Class<? extends DependantConfiguration<T>> conf, T param, Consumer<AnnotationConfigApplicationContext> consumer) {
        return getMatched(param).findAny().map(IdeDependants::activate)
                .orElseGet(() -> {
                    start(context -> {
                        final Map<String, Object> map = ImmutableMap.of(PARAM, param);
                        final MapPropertySource propertySource = new MapPropertySource("paramMap", map);
                        context.getEnvironment().getPropertySources().addFirst(propertySource);
                        context.register(conf);
                        consumer.accept(context);
                    });
                    return false;
                });
    }

    private static Stream<AnnotationConfigApplicationContext> getMatched(Object param) {
        return CONTEXTS.stream().filter(c -> param.equals(c.getEnvironment().getProperty(PARAM, Object.class)));
    }

    private static boolean activate(ApplicationContext context) {
        context.getBeansOfType(IdeTab.class, false, false).forEach((name, tab) -> {
            final SelectionModel<Tab> selectionModel = tab.getTabPane().getSelectionModel();
            selectionModel.select(tab);
        });
        context.getBeansOfType(Window.class, false, false).forEach((name, win) -> win.requestFocus());
        context.getBeansOfType(Dialog.class, false, false).forEach((name, win) -> win.show());
        return true;
    }

    @Override
    public String toString() {
        return parent.toString();
    }
}
