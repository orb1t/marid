/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.ide.swing.impl.widgets;

import org.marid.ide.itf.Widget;
import org.marid.l10n.Localized.S;

import javax.swing.*;
import java.util.prefs.Preferences;

import static org.marid.methods.PrefMethods.preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class AbstractSwingWidget extends JInternalFrame implements Widget {

    private static final long serialVersionUID = 2475350478673235367L;
    protected final Preferences pref;

    public AbstractSwingWidget(String title, String name) {
        super(S.l(title), true, true, true, true);
        pref = preferences("widgets", name);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setName(name);
    }
}
