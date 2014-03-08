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

import javax.swing.*;
import javax.swing.border.TitledBorder;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractTitledPanel<V> extends JPanel implements TitledInputControl<V> {

    private final TitledBorder titledBorder = new TitledBorder(BorderFactory.createEtchedBorder());

    public AbstractTitledPanel() {
        titledBorder.setTitlePosition(TitledBorder.CENTER);
        setBorder(BorderFactory.createCompoundBorder(titledBorder, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
    }

    @Override
    public String getTitle() {
        return titledBorder.getTitle();
    }

    @Override
    public void setTitle(String title) {
        titledBorder.setTitle(title);
    }
}
