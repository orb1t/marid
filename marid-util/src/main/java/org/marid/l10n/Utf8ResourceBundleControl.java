/*-
 * #%L
 * marid-util
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

package org.marid.l10n;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Dmitry Ovchinnikov
 */
public class Utf8ResourceBundleControl extends ResourceBundle.Control {

  public static final Utf8ResourceBundleControl UTF8CTRL = new Utf8ResourceBundleControl();

  private Utf8ResourceBundleControl() {
  }

  @Override
  public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader classLoader, boolean reload) throws IOException {
    final String resource = toResourceName(toBundleName(baseName, locale), "properties");
    return new ChainedPropertyResourceBundle(classLoader.resources(resource).toArray(URL[]::new), !reload);
  }

  @Override
  public List<String> getFormats(String baseName) {
    return FORMAT_PROPERTIES;
  }
}
