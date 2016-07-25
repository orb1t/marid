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

package org.marid.jfx.controls;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public class IdeTab extends Tab {

    @Resource
    protected TabPane ideTabPane;

    @Resource
    protected AnnotationConfigApplicationContext context;

    public IdeTab(Node content, String text, Object...args) {
        super(s(text, args), content);
    }

    public IdeTab(String text, Object... args) {
        super(s(text, args));
    }

    public IdeTab() {
    }

    @PostConstruct
    private void init() {
        ideTabPane.getTabs().add(this);
        ideTabPane.getSelectionModel().select(this);
        final EventHandler<Event> oldClosed = getOnClosed();
        setOnClosed(event -> {
            if (oldClosed != null) {
                oldClosed.handle(event);
            }
            context.close();
        });
    }

    @PreDestroy
    private void destroy() {
        ideTabPane.getTabs().remove(this);
    }
}
