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

package org.marid.swing.menu;

import images.Images;
import org.marid.logging.LogSupport;
import org.marid.methods.LogMethods;
import org.marid.swing.MaridAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import java.util.logging.Logger;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 */
public class ActionBuilder extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(ActionBuilder.class.getName());
    private MaridAction.MaridActionListener actionListener;
    private Consumer<Action> actionInitializer;

    ActionBuilder(String name) {
        super(s(name));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (actionListener != null) {
            try {
                actionListener.actionPerformed(this, e);
            } catch (Exception x) {
                if (e.getSource() instanceof Component) {
                    final Window window = SwingUtilities.windowForComponent((Component) e.getSource());
                    if (window instanceof LogSupport) {
                        ((LogSupport) window).warning("Action {0} error", x, getValue(NAME));
                        return;
                    }
                }
                LogMethods.warning(LOG, "Action {0} error", x, getValue(NAME));
            }
        }
    }

    public ActionBuilder setIcon(String icon) {
        final ImageIcon smallIcon = Images.getIcon(icon + "16.png", 16);
        if (smallIcon == null) {
            final ImageIcon stdIcon = Images.getIcon(icon + ".png", 16);
            if (stdIcon != null) {
                putValue(SMALL_ICON, stdIcon);
            }
        } else {
            putValue(SMALL_ICON, smallIcon);
        }
        final ImageIcon largeIcon = Images.getIcon(icon + "24.png", 24);
        if (largeIcon != null) {
            putValue(LARGE_ICON_KEY, largeIcon);
        }
        return this;
    }

    public ActionBuilder setListener(MaridAction.MaridActionListener listener) {
        this.actionListener = listener;
        return this;
    }

    public ActionBuilder setListener(ActionListener listener) {
        return setListener((a, e) -> listener.actionPerformed(e));
    }

    public ActionBuilder setShortDescription(String shortDescription) {
        putValue(SHORT_DESCRIPTION, s(shortDescription));
        return this;
    }

    public ActionBuilder setLongDescription(String longDescription) {
        putValue(LONG_DESCRIPTION, s(longDescription));
        return this;
    }

    public ActionBuilder setKey(String key) {
        putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(key));
        return this;
    }

    public ActionBuilder setInitializer(Consumer<Action> actionInitializer) {
        this.actionInitializer = actionInitializer;
        return this;
    }

    @Override
    public Object getValue(String key) {
        switch (key) {
            case SHORT_DESCRIPTION:
                final Object v = super.getValue(key);
                return v == null ? super.getValue(NAME) : v;
            default:
                return super.getValue(key);
        }
    }

    @Override
    public boolean isEnabled() {
        if (actionInitializer != null) {
            try {
                actionInitializer.accept(this);
            } finally {
                actionInitializer = null;
            }
        }
        return super.isEnabled();
    }
}
