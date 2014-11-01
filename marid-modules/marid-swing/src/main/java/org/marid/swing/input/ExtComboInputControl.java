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

package org.marid.swing.input;

import org.marid.swing.actions.MaridAction;
import org.marid.swing.pref.Configurable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Vector;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class ExtComboInputControl<E extends Configurable> extends JPanel implements InputControl<E> {

    private final ComboInputControl<E> comboInputControl;

    private ExtComboInputControl(ComboInputControl<E> comboInputControl) {
        super(new BorderLayout());
        add(this.comboInputControl = comboInputControl);
        add(new JButton(new MaridAction(s("Edit"), "configuration", e -> {
            final E selected = comboInputControl.getInputValue();
            final JDialog dialog = selected.createConfigurationDialog(SwingUtilities.windowForComponent(this));
            if (dialog != null) {
                dialog.setVisible(true);
            }
        })), BorderLayout.EAST);
    }

    public ExtComboInputControl(E[] values) {
        this(new ComboInputControl<>(values));
    }

    public ExtComboInputControl(Collection<E> values) {
        this(new ComboInputControl<>(values));
    }

    public ExtComboInputControl(Vector<E> values) {
        this(new ComboInputControl<>(values));
    }

    public ExtComboInputControl(Class<E> type, Class<?> constantContainer) {
        this(new ComboInputControl<>(type, constantContainer));
    }

    public ExtComboInputControl(Class<E> type) {
        this(new ComboInputControl<>(type));
    }

    @Override
    public E getInputValue() {
        return comboInputControl.getInputValue();
    }

    @Override
    public void setInputValue(E value) {
        comboInputControl.setInputValue(value);
    }

    @Override
    public int getBaseline(int width, int height) {
        return comboInputControl.getBaseline(width, height);
    }

    @Override
    public BaselineResizeBehavior getBaselineResizeBehavior() {
        return comboInputControl.getBaselineResizeBehavior();
    }
}
