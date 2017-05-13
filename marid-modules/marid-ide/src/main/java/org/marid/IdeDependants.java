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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
@Component("dependants")
public class IdeDependants {

    private static final Collection<WeakReference<GenericApplicationContext>> CONTEXTS = new ConcurrentLinkedQueue<>();

    private final GenericApplicationContext parent;

    @Autowired
    public IdeDependants(GenericApplicationContext parent) {
        this.parent = parent;
    }

    public GenericApplicationContext start(Consumer<AnnotationConfigApplicationContext> consumer) {
        final AnnotationConfigApplicationContext context = new DependantContext(parent);
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
        return CONTEXTS.stream()
                .map(Reference::get)
                .filter(Objects::nonNull)
                .filter(c -> {
                    if (c.containsBean("params")) {
                        final Object params = c.getBean("params");
                        return params.equals(param);
                    } else {
                        return false;
                    }
                })
                .findAny()
                .map(IdeDependants::activate)
                .orElseGet(() -> start(context -> {
                    context.getBeanFactory().registerSingleton("params", param);
                    context.register(conf);
                    consumer.accept(context);
                }));
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

    static class MainContext extends AnnotationConfigApplicationContext {

        MainContext() {
            getBeanFactory().addBeanPostProcessor(new MaridCommonPostProcessor());
            setAllowBeanDefinitionOverriding(false);
            setAllowCircularReferences(false);
        }

        @Override
        protected void onClose() {
            for (final WeakReference<GenericApplicationContext> ref : CONTEXTS) {
                final GenericApplicationContext c = ref.get();
                if (c != null && c.getBeanFactory().getParentBeanFactory() == getBeanFactory()) {
                    c.close();
                    return;
                }
            }
        }
    }

    private static class DependantContext extends MainContext {

        private final WeakReference<GenericApplicationContext> ref = new WeakReference<>(this);

        private DependantContext(GenericApplicationContext parent) {
            getBeanFactory().addBeanPostProcessor(new WindowAndDialogPostProcessor(this));
            getBeanFactory().setParentBeanFactory(parent.getDefaultListableBeanFactory());
            register(IdeDependants.class);
        }

        @Override
        protected void onRefresh() throws BeansException {
            CONTEXTS.removeIf(c -> c.get() == null);
            CONTEXTS.add(ref);
        }

        @Override
        protected void onClose() {
            CONTEXTS.removeIf(c -> c.get() == null || c == ref);
            super.onClose();
        }

        @Override
        protected void finalize() throws Throwable {
            Platform.runLater(this::close);
        }
    }
}
