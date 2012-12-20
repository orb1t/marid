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
package org.marid.ide;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import org.marid.logging.Logging;

/**
 * Main IDE class.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class MaridIde {

	static {
		Class<?> c = MaridIde.class;
		try (InputStream is = c.getResourceAsStream("/ide.properties")) {

		} catch (Exception x) {

		}
	}

	/**
	 * Entry point.
	 * @param args Command-line arguments.
	 */
	public static void main(String... args) {
		Logging.init(MaridIde.class, "res.messages");
		try {
			UIManager.setLookAndFeel(new NimbusLookAndFeel());
		} catch (Exception x) {
			Log.l.log(Level.WARNING, "Unsupported look and feel", x);
		}
	}

	private static class Log {
		private static final Logger l = Logger.getLogger("ide", "res.messages");
	}
}
