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
package org.marid.ide.res;

import images.Images;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import org.marid.l10n.Localized;

/**
 * Marid action.
 * 
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public abstract class MaridAction extends AbstractAction implements Localized {
    /**
     * Constructs a marid action.
     * @param name Action name.
     * @param info Action info (tooltip).
     * @param icon Action icon path.
     * @param key Action key.
     * @param enabled Enabled state.
     * @param size Icon size.
     */
    public MaridAction(String name, String info, String icon,
            String key, Boolean enabled, int size) {
        if (name != null) {
            putValue(NAME, S.l(name));
        }
        if (icon != null) {
            putValue(SMALL_ICON, Images.getIcon(icon, size, size));
        }
        if (enabled != null) {
            setEnabled(enabled);
        }
        if (info != null) {
            putValue(SHORT_DESCRIPTION, S.l(info));
        }
        if (key != null){
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(key));
        }
    }
    
    /**
     * Constructs a marid action (with 16x16 icon).
     * @param name Action name.
     * @param info Action info.
     * @param icon Action icon path.
     * @param key Action key.
     * @param enabled Enabled state.
     */
    public MaridAction(String name, String info, String icon,
            String key, Boolean enabled) {
        this(name, info, icon, key, enabled, 16);
    }
    
    /**
     * Constructs a marid action (with 16x16 icon).
     * @param name Action name.
     * @param info Action info.
     * @param icon Action icon path.
     * @param key Action key.
     */
    public MaridAction(String name, String info, String icon, String key) {
        this(name, info, icon, key, null);
    }
    
    /**
     * Constructs a marid action (with 16x16 icon).
     * @param name Action name.
     * @param info Action info.
     * @param icon Action icon path.
     */
    public MaridAction(String name, String info, String icon) {
        this(name, info, icon, null);
    }
    
    /**
     * Constructs a marid action.
     * @param name Action name.
     * @param info Action info.
     */
    public MaridAction(String name, String info) {
        this(name, info, null);
    }
    
    /**
     * Constructs a marid action.
     * @param name Action name.
     */
    public MaridAction(String name) {
        this(name, null);
    }
    
    /**
     * Default constructor.
     */
    public MaridAction() {
    }
}
