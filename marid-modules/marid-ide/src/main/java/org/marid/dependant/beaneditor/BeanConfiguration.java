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

package org.marid.dependant.beaneditor;

import org.marid.beans.IdeBean;
import org.marid.ide.project.ProjectFileType;
import org.marid.ide.project.ProjectProfile;
import org.marid.io.Xmls;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

@Configuration
@ComponentScan
public class BeanConfiguration {

	private final ProjectProfile profile;

	public BeanConfiguration(ProjectProfile profile) {
		this.profile = profile;
	}

	@Bean
	public ProjectProfile profile() {
		return profile;
	}

	@Bean
	public IdeBean root() {
		final Path beansFile = profile.get(ProjectFileType.BEANS_XML);
		if (Files.isRegularFile(beansFile)) {
			try {
				return Xmls.read(beansFile, IdeBean::new);
			} catch (Exception x) {
				log(WARNING, "Unable to read {0}", x, beansFile);
			}
		}
		return new IdeBean();
	}

	@Override
	public int hashCode() {
		return profile.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this || (obj instanceof BeanConfiguration && ((BeanConfiguration) obj).profile.equals(profile));
	}
}
