/*
 * Copyright (c) 2017 Dmitry Ovchinnikov
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

package org.marid.dependant.beantree.items;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.marid.ide.common.SpecialActions;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.misc.Casts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractTreeItem<T> extends TreeItem<Object> {

    public final T elem;
    public final Map<String, FxAction> actionMap = new TreeMap<>();
    public final SimpleBooleanProperty focused = new SimpleBooleanProperty();
    public final AtomicReference<Consumer<ObservableList<MenuItem>>> menu = new AtomicReference<>(i -> {});
    public final List<Runnable> destroyActions = new ArrayList<>();

    public AbstractTreeItem(T elem) {
        this.elem = elem;
        parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null) {
                focused.set(false);
            }
        });
    }

    public abstract ObservableValue<String> getName();

    public abstract ObservableValue<String> getType();

    public abstract ObservableValue<Node> valueGraphic();

    public abstract ObservableValue<String> valueText();

    public void edit(TreeTableView<Object> view, Object value) {
    }

    public <I extends AbstractTreeItem<?>> I find(Class<I> type) {
        for (TreeItem<?> i = this; i != null; i = i.getParent()) {
            if (type.isInstance(i)) {
                return type.cast(i);
            }
        }
        throw new NoSuchElementException("No such element of " + type);
    }

    public ProjectProfile getProfile() {
        return find(ProjectTreeItem.class).elem;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + elem + "]";
    }

    @Autowired
    private void init(GenericApplicationContext context, SpecialActions specialActions) {
        final DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
        final ObservableList<TreeItem<Object>> children = getChildren();
        children.forEach(o -> {
            beanFactory.initializeBean(o, null);
            beanFactory.autowireBean(o);
        });
        final ListChangeListener<TreeItem<Object>> onChildrenChange = c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(o -> {
                        beanFactory.initializeBean(o, null);
                        beanFactory.autowireBean(o);
                    });
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(beanFactory::destroyBean);
                }
            }
        };
        final ApplicationListener<ContextClosedEvent> closeListener = e -> {
            children.forEach(beanFactory::destroyBean);
            beanFactory.destroyBean(this);
        };
        context.addApplicationListener(closeListener);
        children.addListener(onChildrenChange);
        destroyActions.add(() -> {
            context.getApplicationListeners().remove(closeListener);
            children.removeListener(onChildrenChange);
        });
        focused.addListener((observable, oldValue, newValue) -> actionMap.forEach((key, value) -> {
            final FxAction action = specialActions.getAction(key);
            if (action != null) {
                value.copy(action, newValue);
            }
        }));
    }

    @PreDestroy
    private void destroy() {
        destroyActions.forEach(Runnable::run);
        destroyActions.clear();
    }

    protected static class ListSynchronizer<F, T extends AbstractTreeItem<F>> implements ListChangeListener<F> {

        private final ObservableList<F> source;
        private final ObservableList<T> target;
        private final Function<F, T> mapper;
        private final WeakListChangeListener<F> sourceChangeListener;

        protected ListSynchronizer(ObservableList<F> source, ObservableList<? super T> target, Function<F, T> mapper) {
            this.source = source;
            this.target = Casts.cast(target);
            this.mapper = mapper;
            source.stream().map(mapper).forEach(target::add);
            sort();
            source.addListener(sourceChangeListener = new WeakListChangeListener<>(this));
        }

        @Override
        public void onChanged(Change<? extends F> c) {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().stream().map(mapper).forEach(target::add);
                    sort();
                } else if (c.wasRemoved()) {
                    target.removeIf(e -> c.getRemoved().contains(e.elem));
                }
            }
        }

        private void sort() {
            if (target.stream().allMatch(Comparable.class::isInstance)) {
                target.sort(Casts.cast(Comparator.naturalOrder()));
            }
        }

        protected void destroy() {
            source.removeListener(sourceChangeListener);
        }
    }
}
