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

package org.marid.ide.toolbar;

import de.jensd.fx.glyphs.GlyphIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;
import org.marid.ee.IdeSingleton;
import org.marid.ide.icons.IdeIcons;
import org.marid.ide.menu.IdeMenuItem;
import org.marid.logging.LogSupport;
import org.marid.util.Utils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.TransientReference;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@IdeSingleton
public class IdeToolbar extends ToolBar implements LogSupport {

    public IdeToolbar() {
        setMaxWidth(Double.MAX_VALUE);
    }

    @Inject
    private void init(BeanManager beanManager, @TransientReference @IdeToolbarItem Instance<Node> nodes) {
        final Type type = new ParameterizedTypeImpl(EventHandler.class, ActionEvent.class);
        final Set<Bean<EventHandler<ActionEvent>>> beans = Utils.cast(beanManager.getBeans(type, AnyLiteral.INSTANCE));
        final Map<String, Set<Node>> buttonMap = new TreeMap<>();
        for (final Bean<EventHandler<ActionEvent>> bean : beans) {
            try {
                final IdeToolbarItem ti = bean.getQualifiers().stream()
                        .filter(IdeToolbarItem.class::isInstance)
                        .map(IdeToolbarItem.class::cast)
                        .findAny()
                        .orElse(null);
                if (ti == null) {
                    continue;
                }
                final IdeMenuItem mi = bean.getQualifiers().stream()
                        .filter(IdeMenuItem.class::isInstance)
                        .map(IdeMenuItem.class::cast)
                        .findAny()
                        .orElse(null);
                final String group = ti.group().isEmpty() ? (mi != null ? mi.group() : ti.group()) : ti.group();
                final String id = ti.id().isEmpty() ? null : ti.id();
                final String tip = ti.tip().isEmpty() ? (mi != null ? mi.text() : null) : ti.tip();
                final GlyphIcon<?> toolbarIcon = Stream.of(ti.faIcons(), ti.mdIcons(), ti.mIcons(), ti.oIcons(), ti.wIcons())
                        .flatMap(Stream::of)
                        .findAny()
                        .map(e -> IdeIcons.glyphIcon(e, 16))
                        .orElse(null);
                final GlyphIcon<?> menuIcon;
                if (mi == null || toolbarIcon != null) {
                    menuIcon = null;
                } else {
                    menuIcon = Stream.of(mi.faIcons(), mi.mdIcons(), mi.mIcons(), mi.oIcons(), mi.wIcons())
                            .flatMap(Stream::of)
                            .findAny()
                            .map(e -> IdeIcons.glyphIcon(e, 16))
                            .orElse(null);
                }
                final GlyphIcon<?> icon = toolbarIcon == null ? menuIcon : toolbarIcon;
                final Button button = new Button(null, icon);
                if (tip != null) {
                    button.setTooltip(new Tooltip(tip));
                }
                if (id != null) {
                    button.setId(id);
                }
                final CreationalContext<EventHandler<ActionEvent>> context = beanManager.createCreationalContext(bean);
                button.setOnAction(bean.create(context));
                buttonMap.computeIfAbsent(group, k -> new LinkedHashSet<>()).add(button);
            } catch (Exception x) {
                log(WARNING, "Unable to create a toolbar item {0}", x, bean);
            }
        }
        nodes.forEach(node -> {
            final String group = Optional.ofNullable(node.getProperties().get("group")).map(String::valueOf).orElse("");
            buttonMap.computeIfAbsent(group, k -> new LinkedHashSet<>()).add(node);
        });
        buttonMap.forEach((group, buttons) -> {
            getItems().addAll(buttons);
            getItems().add(new Separator());
        });
    }
}
