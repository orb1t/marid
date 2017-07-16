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

import javafx.beans.value.ObservableStringValue;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.LocalizedStrings;
import org.marid.spring.dependant.DependantConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

import static org.marid.ide.common.IdeShapes.circle;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@ComponentScan
public class BeanEditorConfiguration extends DependantConfiguration<BeanEditorParam> {

    @Bean
    public ProjectProfile profile() {
        return param.profile;
    }

    @Bean
    public ObservableStringValue beanEditorTabText() {
        return LocalizedStrings.ls("Beans");
    }

    @Bean
    public Supplier<Node> beanEditorGraphic(ProjectProfile profile) {
        return () -> new HBox(3, circle(profile.hashCode(), 16));
    }
}
