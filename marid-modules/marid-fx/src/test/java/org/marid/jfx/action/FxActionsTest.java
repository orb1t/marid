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

package org.marid.jfx.action;

import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.marid.jfx.menu.MaridMenu;
import org.marid.test.NormalTests;
import org.mockito.ArgumentCaptor;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.awt.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;

/**
 * @author Dmitry Ovchinnikov.
 */
@Category({NormalTests.class})
@RunWith(MockitoJUnitRunner.class)
public class FxActionsTest {

    @Spy
    private TestActions testActions;

    private MaridMenu menu;

    @BeforeClass
    public static void initFX() throws Exception {
        EventQueue.invokeAndWait(JFXPanel::new);
    }

    @Before
    public void init() {
        menu = testActions.createMenu();
    }

    @Test
    public void testA1() {
        final Menu m = menu.getMenus().stream().filter(e -> "#menu1".equals(e.getText())).findAny().orElse(null);
        assertNotNull(m);
        final MenuItem item = m.getItems().stream().filter(i -> "#a1".equals(i.getText())).findAny().orElse(null);
        assertNotNull(item);
        item.getOnAction().handle(new ActionEvent(item, item));
        verify(testActions).action1();
    }

    @Test
    public void testA2() {
        final Menu m = menu.getMenus().stream().filter(e -> "#menu1".equals(e.getText())).findAny().orElse(null);
        assertNotNull(m);
        final MenuItem item = m.getItems().stream().filter(i -> "#a2".equals(i.getText())).findAny().orElse(null);
        assertNotNull(item);
        final ActionEvent event = new ActionEvent(item, item);
        item.getOnAction().handle(event);
        verify(testActions).action2(same(event));
    }

    @Test
    public void testA3() {
        final Menu m = menu.getMenus().stream().filter(e -> "#menu1".equals(e.getText())).findAny().orElse(null);
        assertNotNull(m);
        final MenuItem item = m.getItems().stream().filter(i -> "#a3".equals(i.getText())).findAny().orElse(null);
        assertNotNull(item);
        final ActionEvent event = new ActionEvent(item, item);
        item.getOnAction().handle(event);
        final ArgumentCaptor<FxAction> argumentCaptor = ArgumentCaptor.forClass(FxAction.class);
        verify(testActions).action3(argumentCaptor.capture(), same(event));
        assertEquals("#menu1", argumentCaptor.getValue().getMenu());
        assertEquals("#a3", argumentCaptor.getValue().getText());
        verify(testActions).action3(argumentCaptor.capture(), same(event));
    }

    @Test
    public void testA4() {
        final Menu m = menu.getMenus().stream().filter(e -> "#menu2".equals(e.getText())).findAny().orElse(null);
        assertNotNull(m);
        final MenuItem item = m.getItems().stream().filter(i -> "#a4".equals(i.getText())).findAny().orElse(null);
        assertNotNull(item);
        item.getOnAction().handle(new ActionEvent(item, item));
        verify(testActions).action4();
    }

    @Test
    public void testA5() {
        final Menu m = menu.getMenus().stream().filter(e -> "#menu3".equals(e.getText())).findAny().orElse(null);
        assertNotNull(m);
        final MenuItem item = m.getItems().stream().filter(i -> "#a5".equals(i.getText())).findAny().orElse(null);
        assertNotNull(item);
        final ArgumentCaptor<FxAction> argumentCaptor = ArgumentCaptor.forClass(FxAction.class);
        final ActionEvent event = new ActionEvent(item, item);
        item.getOnAction().handle(event);
        verify(testActions).action5(argumentCaptor.capture(), same(event));
        final FxAction fxAction = argumentCaptor.getValue();
        assertTrue(fxAction.selectedProperty().get());
    }

    public static class TestActions extends FxActions {

        @Action(menu = "#menu1", name = "#a1")
        public void action1() {
        }

        @Action(menu = "#menu1", name = "#a2")
        public void action2(ActionEvent event) {
        }

        @Action(menu = "#menu1", name = "#a3")
        public void action3(FxAction action, ActionEvent event) {
        }

        @Action
        public FxAction action4() {
            return new FxAction("tg1", "g1", "#menu2")
                    .setEventHandler(event -> {})
                    .setText("#a4");
        }

        @Action(menu = "#menu3", name = "#a5", conf = Conf5.class)
        public void action5(FxAction action, ActionEvent event) {
        }

        public class Conf5 implements ActionConfigurer {

            @Override
            public void configure(FxAction action) {
                action.setSelected(true);
            }
        }
    }
}
