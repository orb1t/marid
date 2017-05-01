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

import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableView;
import org.marid.dependant.beaneditor.ValueMenuItems;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.FxAction;
import org.marid.misc.Casts;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;

import java.util.Comparator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractTreeItem<T> extends TreeItem<Object> {

    public final T elem;
    public final Map<String, FxAction> actionMap = new TreeMap<>();

    public AbstractTreeItem(T elem) {
        this.elem = elem;
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

    public ValueMenuItems valueMenuItems(AutowireCapableBeanFactory beanFactory) {
        return null;
    }

    protected static class ListSynchronizer<F, T extends AbstractTreeItem<F>> implements ListChangeListener<F> {

        private final ObservableList<T> target;
        private final Function<F, T> mapper;

        protected ListSynchronizer(ObservableList<F> source, ObservableList<? super T> target, Function<F, T> mapper) {
            this.target = Casts.cast(target);
            this.mapper = mapper;
            source.stream().map(mapper).forEach(target::add);
            sort();
            source.addListener(new WeakListChangeListener<>(this));
        }

        @Override
        public void onChanged(Change<? extends F> c) {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().stream().map(mapper).forEach(target::add);
                    sort();
                } else if (c.wasRemoved()) {
                    target.removeIf(e -> c.getList().contains(e.elem));
                }
            }
        }

        private void sort() {
            if (target.stream().allMatch(Comparable.class::isInstance)) {
                target.sort(Casts.cast(Comparator.naturalOrder()));
            }
        }
    }
}
