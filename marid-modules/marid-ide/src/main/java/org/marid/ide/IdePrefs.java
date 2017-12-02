/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.marid.ide;

import org.marid.ide.logging.IdeLogConsoleHandler;
import org.marid.ide.logging.IdeLogHandler;

import java.util.Locale;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdePrefs {

  public static final Preferences PREFERENCES = Preferences.userNodeForPackage(IdePrefs.class).node("Ide");

  static {
    // locale
    final String locale = PREFERENCES.get("locale", null);
    if (locale != null) {
      Locale.setDefault(Locale.forLanguageTag(locale));
    }

    // logging
    LogManager.getLogManager().reset();
    Logger.getLogger("").addHandler(new IdeLogHandler());
    Logger.getLogger("").addHandler(new IdeLogConsoleHandler());
    Thread.setDefaultUncaughtExceptionHandler((t, e) -> log(WARNING, "Exception in {0}", e, t));
  }
}
