/*-
 * #%L
 * marid-runtime
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

package org.marid.runtime.context;

import org.marid.runtime.exception.MaridCircularPlaceholderException;

import java.util.LinkedHashSet;
import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov
 */
public class MaridPlaceholderResolver {

	private final ClassLoader classLoader;
	private final Properties properties;

	public MaridPlaceholderResolver(ClassLoader classLoader, Properties properties) {
		this.classLoader = classLoader;
		this.properties = properties;
	}

	public MaridPlaceholderResolver(Properties properties) {
		this(Thread.currentThread().getContextClassLoader(), properties);
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public Properties getProperties() {
		return properties;
	}

	public String resolvePlaceholders(String value) {
		if (value == null || value.isEmpty() || value.indexOf('$') < 0) {
			return value;
		} else {
			return resolvePlaceholders(new LinkedHashSet<>(), value);
		}
	}

	private String resolvePlaceholders(LinkedHashSet<String> passed, String value) {
		if (value == null || value.isEmpty() || value.indexOf('$') < 0) {
			return value;
		} else {
			final StringBuilder builder = new StringBuilder(value);
			for (int i = 0; i < builder.length(); ) {
				if (builder.charAt(i) == '$') {
					if (i < builder.length() - 1) {
						if (builder.charAt(i + 1) == '{') {
							final int closeIndex = builder.indexOf("}", i + 1);
							if (closeIndex < 0) {
								return builder.toString();
							} else {
								final String placeholder = builder.substring(i + 2, closeIndex);
								final int defIndex = placeholder.lastIndexOf(':');
								final String name;
								final String defValue;
								if (defIndex < 0) {
									name = placeholder;
									defValue = "";
								} else {
									name = placeholder.substring(0, defIndex);
									defValue = placeholder.substring(defIndex + 1);
								}
								final String toReplace = String.valueOf(resolvePlaceholder(passed, name, defValue));
								builder.replace(i, closeIndex + 1, toReplace);
								i += toReplace.length();
							}
						} else {
							i++;
						}
					} else {
						i++;
					}
				} else {
					i++;
				}
			}
			return builder.toString();
		}
	}

	private String resolvePlaceholder(LinkedHashSet<String> passed, String name, String defValue) {
		if (passed.add(name)) {
			final String result = resolvePlaceholders(passed, properties.getProperty(name, defValue));
			passed.remove(name);
			return result;
		} else {
			throw new MaridCircularPlaceholderException(passed, name);
		}
	}

}
