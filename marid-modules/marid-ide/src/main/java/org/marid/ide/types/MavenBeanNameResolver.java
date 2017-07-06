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

package org.marid.ide.types;

import com.github.javaparser.ast.type.Type;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.lang.reflect.Field;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class MavenBeanNameResolver implements BeanNameResolver {

    private final ProjectManager projectManager;
    private final CompilerConfiguration compilerConfiguration;

    @Autowired
    public MavenBeanNameResolver(ProjectManager projectManager) {
        this.projectManager = projectManager;
        this.compilerConfiguration = new CompilerConfiguration();
        this.compilerConfiguration.setJointCompilationOptions(new HashMap<>());
        this.compilerConfiguration.setSourceEncoding("UTF-8");
        this.compilerConfiguration.setTargetBytecode(CompilerConfiguration.JDK8);
    }

    @Override
    public SortedSet<String> beanNames(Path javaFile, Type type) {
        final ProjectProfile profile = projectManager.getProfile(javaFile).orElse(null);
        if (profile == null) {
            return Collections.emptySortedSet();
        }
        final URLClassLoader urlClassLoader = profile.classPath();
        final GroovyClassLoader loader = new GroovyClassLoader(urlClassLoader, compilerConfiguration);
        try {
            loader.addURL(profile.getSrcMainJava().toUri().toURL());
        } catch (Exception x) {
            log(WARNING, "Unable to add urls", x);
        }
        final DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        final AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(beanFactory);
        try (final BufferedReader r = Files.newBufferedReader(profile.getBeanClassesFile(), UTF_8)) {
            while (true) {
                final String line = r.readLine();
                if (line == null) {
                    break;
                }
                final String className = line.trim();
                if (!className.isEmpty()) {
                    try {
                        final Class<?> c = loader.loadClass(className, true, true, true);
                        reader.register(c);
                    } catch (Throwable x) {
                        log(WARNING, "Unable to load {0}", x, className);
                    }
                }
            }
        } catch (Exception x) {
            log(WARNING, "Unable to open {0}", x, profile.getBeanClassesFile());
        }
        try {
            final Class<?> injectee = loader.parseClass(classCode(type), "InjectedClass.groovy");
            final Field[] fields = injectee.getFields();
            if (fields.length == 1) {
                final ResolvableType resolvableType = ResolvableType.forField(fields[0]);
                return new TreeSet<>(Arrays.asList(beanFactory.getBeanNamesForType(resolvableType)));
            }
        } catch (Throwable x) {
            log(WARNING, "Unable to compile groovy source", x);
        }
        return Collections.emptySortedSet();
    }

    private String classCode(Type type) {
        final StringBuilder builder = new StringBuilder();
        try (final Formatter formatter = new Formatter(builder)) {
            formatter.format("class InjectedClass {%n");
            formatter.format("  public %s field;%n", type.asString());
            formatter.format("}%n");
        }
        return builder.toString();
    }
}
