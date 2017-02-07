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

package org.marid.ide.tabs;

import javafx.beans.Observable;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import org.marid.jfx.props.Props;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.*;
import java.util.function.Supplier;

import static javafx.beans.binding.Bindings.createObjectBinding;

/**
 * @author Dmitry Ovchinnikov.
 */
public class IdeTab extends Tab {

    private final IdeTabKey key;

    @Resource
    protected transient IdeTabPane ideTabPane;

    @Resource
    protected transient GenericApplicationContext context;

    private final List<Supplier<Node>> suppliers = new ArrayList<>();
    private final List<Observable> nodeObservables = new ArrayList<>();
    private final List<ObservableValue<String>> texts = new ArrayList<>();

    public IdeTab(@Nonnull Node content, @Nonnull ObservableValue<String> text, @Nonnull Supplier<Node> node) {
        setContent(content);
        textProperty().bind(text);
        this.key = new IdeTabKey(text, node);
    }

    public void addNodeObservables(Observable... observables) {
        Collections.addAll(nodeObservables, observables);
    }

    @EventListener
    private void init(ContextStartedEvent contextStartedEvent) {
        final DefaultListableBeanFactory f = context.getDefaultListableBeanFactory();
        for (DefaultListableBeanFactory c = f; c != null; c = (DefaultListableBeanFactory) c.getParentBeanFactory()) {
            final Map<String, IdeTab> tabMap = c.getBeansOfType(IdeTab.class);
            for (final Map.Entry<String, IdeTab> tabEntry : tabMap.entrySet()) {
                if (c.containsLocalBean(tabEntry.getKey())) {
                    final IdeTabKey k = tabEntry.getValue().key;
                    suppliers.add(0, k.graphicBinding);
                    texts.add(0, k.textBinding);
                }
            }
        }
        if (suppliers.size() > 1) {
            suppliers.remove(0);
        }
        graphicProperty().bind(createObjectBinding(() -> suppliers.stream().reduce(new HBox(4), (b, e) -> {
            b.getChildren().add(e.get());
            return b;
        }, (b1, b2) -> b2), nodeObservables.toArray(new Observable[nodeObservables.size()])));

        ideTabPane.getTabs().add(this);
        ideTabPane.getSelectionModel().select(this);
        Props.addHandler(onClosedProperty(), event -> context.close());
    }

    @PreDestroy
    private void destroy() {
        ideTabPane.getTabs().remove(this);
    }

    @Override
    public final int hashCode() {
        return key.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        final IdeTab that = (IdeTab) obj;
        final String[] thisKeys = this.texts.stream().map(ObservableValue::getValue).toArray(String[]::new);
        final String[] thatKeys = that.texts.stream().map(ObservableValue::getValue).toArray(String[]::new);
        return Arrays.equals(thisKeys, thatKeys);
    }
}
