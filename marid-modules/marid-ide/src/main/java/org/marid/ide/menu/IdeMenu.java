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

package org.marid.ide.menu;

import de.jensd.fx.glyphs.GlyphIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.KeyCombination;
import org.jboss.weld.literal.AnyLiteral;
import org.jboss.weld.util.reflection.ParameterizedTypeImpl;
import org.marid.ee.IdeSingleton;
import org.marid.ide.icons.IdeIcons;
import org.marid.l10n.L10nSupport;
import org.marid.logging.LogSupport;
import org.marid.util.Utils;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.TransientReference;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@IdeSingleton
public class IdeMenu extends MenuBar implements L10nSupport, LogSupport {

    public IdeMenu() {
        setMaxWidth(Double.MAX_VALUE);
    }

    @Inject
    private void init(BeanManager beanManager, @TransientReference @IdeMenuItem Instance<MenuItem> items) {
        final Type type = new ParameterizedTypeImpl(EventHandler.class, ActionEvent.class);
        final Set<Bean<EventHandler<ActionEvent>>> beans = Utils.cast(beanManager.getBeans(type, AnyLiteral.INSTANCE));
        final Map<String, Map<String, Map<String, MenuItem>>> itemMap = new TreeMap<>();
        for (final Bean<EventHandler<ActionEvent>> bean : beans) {
            try {
                final IdeMenuItem mi = bean.getQualifiers().stream()
                        .filter(IdeMenuItem.class::isInstance)
                        .map(IdeMenuItem.class::cast)
                        .findAny()
                        .orElse(null);
                if (mi == null || mi.menu().isEmpty()) {
                    continue;
                }
                final GlyphIcon<?> icon = Stream.of(mi.faIcons(), mi.mdIcons(), mi.mIcons(), mi.oIcons(), mi.wIcons())
                        .flatMap(Stream::of)
                        .findAny()
                        .map(e -> IdeIcons.glyphIcon(e, 16))
                        .orElse(null);
                final String key = mi.key().isEmpty() ? null : mi.key();
                final String text = mi.text();
                final MenuItem menuItem = new MenuItem(s(text), icon);
                if (key != null) {
                    menuItem.setAccelerator(KeyCombination.valueOf(key));
                }
                final CreationalContext<EventHandler<ActionEvent>> context = beanManager.createCreationalContext(bean);
                menuItem.setOnAction(bean.create(context));
                itemMap
                        .computeIfAbsent(mi.menu(), k -> new TreeMap<>())
                        .computeIfAbsent(mi.group(), k -> new TreeMap<>())
                        .put(mi.text(), menuItem);
            } catch (Exception x) {
                log(WARNING, "Unable to create menu item {0}", x, bean);
            }
        }
        items.forEach(item -> {
            final String group = Optional.ofNullable(item.getProperties().get("group")).map(String::valueOf).orElse("");
            final String menu = Optional.ofNullable(item.getProperties().get("menu")).map(String::valueOf).orElse(null);
            if (menu != null) {
                itemMap
                        .computeIfAbsent(menu, k -> new TreeMap<>())
                        .computeIfAbsent(group, k -> new TreeMap<>())
                        .put(Optional.ofNullable(item.getText()).orElse(""), item);
            }
        });
        itemMap.forEach((menu, groupMap) -> {
            final Menu m = new Menu(s(menu));
            groupMap.forEach((group, menuItems) -> {
                m.getItems().addAll(menuItems.values());
                m.getItems().add(new SeparatorMenuItem());
            });
            if (!m.getItems().isEmpty()) {
                m.getItems().remove(m.getItems().size() - 1);
            }
            getMenus().add(m);
        });
    }
}
