/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package org.marid.bd.components;

import org.marid.bd.Block;
import org.marid.functions.TriConsumer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
public class DefaultBlockComponentEditor<B extends Block> extends AbstractBlockComponentEditor<B> {

    protected final TriConsumer<DefaultBlockComponentEditor<B>, Action, ActionEvent> submitter;

    public DefaultBlockComponentEditor(Window window, B block, Consumer<DefaultBlockComponentEditor<B>> initializer, TriConsumer<DefaultBlockComponentEditor<B>, Action, ActionEvent> submitter) {
        super(window, block);
        initializer.accept(this);
        this.submitter = submitter;
    }

    @Override
    protected void onSubmit(Action action, ActionEvent actionEvent) throws Exception {
        submitter.accept(this, action, actionEvent);
    }

    @Override
    public TabPane tabPane(String tab) {
        return super.tabPane(tab);
    }

    @Override
    public void afterInit() {
        super.afterInit();
    }
}
