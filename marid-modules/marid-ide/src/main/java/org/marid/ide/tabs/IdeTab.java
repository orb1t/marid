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
import javafx.beans.binding.StringBinding;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import org.marid.jfx.props.Props;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * @author Dmitry Ovchinnikov.
 */
public class IdeTab extends Tab {

    private final IdeTabKey key;

    @Resource
    protected transient IdeTabPane ideTabPane;

    @Resource
    protected transient GenericApplicationContext context;

    private final List<Observable> nodeObservables = new ArrayList<>();

    public IdeTab(@Nonnull Node content, @Nonnull StringBinding text, @Nonnull Supplier<Node> node) {
        setContent(content);
        textProperty().bind(text);
        this.key = new IdeTabKey(text, node);
    }

    public void addNodeObservables(Observable... observables) {
        Collections.addAll(nodeObservables, observables);
    }

    private void updateGraphic(Observable observable) {
        final LinkedList<Supplier<Node>> suppliers = new LinkedList<>();
        for (ApplicationContext c = context; c != null; c = c.getParent()) {
            final IdeTabKey k = c.getBean("$ideTabKey", IdeTabKey.class);
            suppliers.addFirst(k.graphicBinding);
        }
        if (suppliers.size() > 1) {
            suppliers.removeFirst();
        }
        setGraphic(suppliers.stream().reduce(new HBox(4), (b, e) -> {
            b.getChildren().add(e.get());
            return b;
        }, (b1, b2) -> b2));
    }

    @PostConstruct
    private void init() {
        context.getBeanFactory().registerSingleton("$ideTabKey", key);
        updateGraphic(this.graphicProperty());
        nodeObservables.forEach(o -> o.addListener(this::updateGraphic));

        final int index = ideTabPane.getTabs().indexOf(this);
        if (index >= 0) {
            ideTabPane.getTabs().remove(index);
            ideTabPane.getTabs().add(index, this);
        } else {
            ideTabPane.getTabs().add(this);
        }
        ideTabPane.getSelectionModel().select(this);
        Props.addHandler(onClosedProperty(), event -> context.close());
    }

    @PreDestroy
    private void destroy() {
        nodeObservables.forEach(o -> o.removeListener(this::updateGraphic));
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
        return this.key.equals(that.key);
    }
}
