/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid.ide.l10n;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Localized resource interface.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface Localized {

    /**
     * Messages.
     */
    public static final ResourceBundle MESSAGES =
            ResourceBundle.getBundle("res.messages");
    /**
     * Strings.
     */
    public static final ResourceBundle STRINGS =
            ResourceBundle.getBundle("res.strings");

    /**
     * Localization utilities class.
     */
    public static class L {

        /**
         * Get a string from strings resource bundle.
         *
         * @param key String key.
         * @param ps String parameters.
         * @return String.
         */
        public static String s(String key, Object... ps) {
            String r;
            try {
                r = STRINGS.getString(key);
            } catch (MissingResourceException x) {
                r = key;
            }
            return ps == null || ps.length == 0 ? r : String.format(r, ps);
        }

        /**
         * Get a message from message resource bundle.
         *
         * @param key Message key.
         * @param v Message parameters.
         * @return Message.
         */
        public static String m(String key, Object... v) {
            String r;
            try {
                r = MESSAGES.getString(key);
            } catch (MissingResourceException x) {
                r = key;
            }
            return v == null || v.length == 0 ? r : MessageFormat.format(r, v);
        }
    }
}
