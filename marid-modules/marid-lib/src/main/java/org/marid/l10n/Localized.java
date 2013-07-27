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

import org.marid.l10n.LocalizationUtils.EmptyResourceBundle;

import java.text.MessageFormat;
import java.util.Formatter;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.*;

/**
 * Localized resource interface.
 *
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public interface Localized {

    public class S {
        
        public static final ResourceBundle SB;
        
        static {
            ResourceBundle rb;
            try {
                rb = getBundle("res.strings", LocalizationUtils.UTF8_CONTROL);
            } catch (Exception x) {
                x.printStackTrace(System.err);
                rb = new EmptyResourceBundle();
            }
            SB = rb;
        }

        public static String l(String key, Object... ps) {
            String r = SB.containsKey(key) ? SB.getString(key) : key;
            return ps == null || ps.length == 0 ? r : String.format(r, ps);
        }

        public static void l(String key, Appendable out, Object... ps) {
            String r = SB.containsKey(key) ? SB.getString(key) : key;
            Formatter formatter = new Formatter(out);
            formatter.format(r, ps);
        }

        public static void l(String key, Formatter formatter, Object... ps) {
            String r = SB.containsKey(key) ? SB.getString(key) : key;
            formatter.format(r, ps);
        }
    }

    public class M {

        public static final ResourceBundle MB;
        
        static {
            ResourceBundle rb;
            try {
                rb = getBundle("res.messages", LocalizationUtils.UTF8_CONTROL);
            } catch (Exception x) {
                x.printStackTrace(System.err);
                rb = new EmptyResourceBundle();
            }
            MB = rb;
        }

        public static String l(String k, Object... v) {
            String r = MB.containsKey(k) ? MB.getString(k) : k;
            return v == null || v.length == 0 ? r : MessageFormat.format(r, v);
        }

        public static void l(String k, StringBuffer buffer, Object... v) {
            String r = MB.containsKey(k) ? MB.getString(k) : k;
            if (v == null || v.length == 0) {
                buffer.append(r);
            } else {
                new MessageFormat(r).format(v, buffer, null);
            }
        }
    }
}
