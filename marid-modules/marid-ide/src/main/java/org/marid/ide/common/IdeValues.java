/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.ide.common;

import javafx.application.Platform;
import org.marid.ide.IdePrefs;
import org.marid.jfx.LocalizedStrings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Locale;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Lazy(false)
public class IdeValues {

	public final String implementationVersion;

	@Autowired
	public IdeValues(@Value("${implementation.version}") String implementationVersion) {
		this.implementationVersion = implementationVersion;
	}

	@PostConstruct
	private void init() {
		IdePrefs.PREFERENCES.addPreferenceChangeListener(evt -> {
			if ("locale".equals(evt.getKey())) {
				final Locale locale = Locale.forLanguageTag(evt.getNewValue());
				if (locale != null && !Locale.ROOT.equals(locale) && !Locale.getDefault().equals(locale)) {
					Locale.setDefault(locale);
					if (Platform.isFxApplicationThread()) {
						LocalizedStrings.LOCALE.set(locale);
					} else {
						Platform.runLater(() -> LocalizedStrings.LOCALE.set(locale));
					}
				}
			}
		});
	}
}
