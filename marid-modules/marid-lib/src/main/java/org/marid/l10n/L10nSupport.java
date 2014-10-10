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

package org.marid.l10n;

import java.util.Locale;
import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public interface L10nSupport {

    default String s(String format, Object... args) {
        return L10n.s(getDefaultL10nLocale(), format, getDefaultLFunc(), args);
    }

    default String m(String format, Object... args) {
        return L10n.m(getDefaultL10nLocale(), format, getDefaultLFunc(), args);
    }

    default Locale getDefaultL10nLocale() {
        return Locale.getDefault();
    }

    default Function<String, String> getDefaultLFunc() {
        return Function.identity();
    }

    class LS {

        public static String s(String format, Object... args) {
            return L10n.s(Locale.getDefault(), format, Function.identity(), args);
        }

        public static String m(String format, Object... args) {
            return L10n.m(Locale.getDefault(), format, Function.identity(), args);
        }
    }
}
