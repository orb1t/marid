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

package org.marid.misc;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Dmitry Ovchinnikov
 */
public interface StringUtils {

	static Predicate<Path> pathEndsWith(String suffix) {
		return p -> p.getFileName().toString().endsWith(suffix);
	}

	static String throwableText(Throwable throwable) {
		final StringWriter writer = new StringWriter();
		try (final PrintWriter w = new PrintWriter(writer)) {
			throwable.printStackTrace(w);
		}
		return writer.toString();
	}

	static String replaceAll(CharSequence text, Pattern pattern, Function<Matcher, String> func) {
		final Matcher matcher = pattern.matcher(text);
		boolean result = matcher.find();
		if (result) {
			final StringBuffer buffer = new StringBuffer();
			do {
				matcher.appendReplacement(buffer, func.apply(matcher));
				result = matcher.find();
			} while (result);
			matcher.appendTail(buffer);
			return buffer.toString();
		} else {
			return text.toString();
		}
	}

	static int count(String string, char c) {
		int count = 0, i = -1;
		while ((i = string.indexOf(c, i + 1)) >= 0) {
			count++;
		}
		return count;
	}
}
