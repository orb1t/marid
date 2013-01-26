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
package org.marid.l10n;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import static java.util.ResourceBundle.getBundle;

/**
 * Localized resource interface.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface Localized {
    /**
     * Strings localizations.
     */
    public class S {

        /**
         * Get a string from strings resource bundle.
         *
         * @param key String key.
         * @param ps String parameters.
         * @return String.
         */
        public static String l(String key, Object... ps) {
            String r = S.containsKey(key) ? S.getString(key) : key;
            return ps == null || ps.length == 0 ? r : String.format(r, ps);
        }

        private static final ResourceBundle S = getBundle("res.strings");
    }

    /**
     * Messages localizations.
     */
    public class M {

        private static final ResourceBundle M = getBundle("res.messages");

        /**
         * Get a message from message resource bundle.
         *
         * @param k Message key.
         * @param v Message parameters.
         * @return Message.
         */
        public static String l(String k, Object... v) {
            String r = M.containsKey(k) ? M.getString(k) : k;
            return v == null || v.length == 0 ? r : MessageFormat.format(r, v);
        }
    }
}
