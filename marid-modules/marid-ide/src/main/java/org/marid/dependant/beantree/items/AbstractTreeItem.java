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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.beans.OCleaner;
import org.marid.misc.Casts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.ResolvableType;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractTreeItem<T> extends TreeItem<Object> implements Comparable<AbstractTreeItem<?>> {

    public final T elem;
    public final Map<String, FxAction> actionMap = new TreeMap<>();
    public final List<Runnable> destroyActions = new ArrayList<>();
    public final LinkedList<Consumer<ObservableList<MenuItem>>> menuConsumers = new LinkedList<>();
    public final ObjectProperty<Node> graphic = new SimpleObjectProperty<>();
    public final ObjectProperty<String> text = new SimpleObjectProperty<>();

    public AbstractTreeItem(T elem) {
        this.elem = elem;
    }

    public abstract ObservableValue<String> getName();

    public abstract ObservableValue<String> getType();

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
    private void init(GenericApplicationContext context) {
        final DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
        final ListChangeListener<TreeItem<Object>> listChangeListener = c -> {
            while (c.next()) {
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(e -> ((AbstractTreeItem<?>) e).destroy(beanFactory));
                }
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(e -> {
                        beanFactory.autowireBean(e);
                        beanFactory.initializeBean(e, null);
                    });
                }
            }
        };
        getChildren().addListener(listChangeListener);
        destroyActions.add(0, () -> getChildren().removeListener(listChangeListener));
    }

    @PreDestroy
    private void destroy() {
        graphic.unbind();
        text.unbind();
        destroyActions.forEach(Runnable::run);
        destroyActions.clear();
    }

    public void destroy(DefaultListableBeanFactory factory) {
        getChildren().clear();
        factory.destroyBean(this);
    }

    public abstract ResolvableType type();

    protected static class ListSynchronizer<F, T extends AbstractTreeItem<F>> implements Runnable {

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
            source.addListener(sourceChangeListener = new WeakListChangeListener<F>(this::onChanged));
        }

        private void onChanged(ListChangeListener.Change<? extends F> c) {
            if (OCleaner.cleaningInProcess) {
                return;
            }
            while (c.next()) {
                if (c.wasReplaced() || c.wasUpdated()) {
                    for (int i = c.getFrom(); i < c.getTo(); i++) {
                        final F from = source.get(i);
                        for (int j = 0; j < target.size(); j++) {
                            final T to = target.get(j);
                            if (to.elem.equals(from)) {
                                target.set(j, mapper.apply(from));
                            }
                        }
                    }
                    sort();
                } else if (c.wasAdded()) {
                    c.getAddedSubList().stream().map(mapper).forEach(target::add);
                    sort();
                } else if (c.wasRemoved()) {
                    target.removeIf(e -> c.getRemoved().contains(e.elem));
                }
            }
        }

        private void sort() {
            target.sort(Comparator.naturalOrder());
        }

        @Override
        public void run() {
            source.removeListener(sourceChangeListener);
        }
    }
}
