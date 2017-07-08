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
import com.google.common.io.MoreFiles;
import org.apache.commons.io.FileUtils;
import org.marid.ide.model.Annotations;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.misc.Urls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import javax.tools.*;
import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.singletonList;
import static java.util.EnumSet.of;
import static java.util.Locale.ENGLISH;
import static java.util.logging.Level.*;
import static java.util.stream.Collectors.toCollection;
import static javax.tools.JavaFileObject.Kind.SOURCE;
import static javax.tools.StandardLocation.*;
import static org.marid.logging.Log.log;
import static org.marid.misc.Urls.jars;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class JavaBeanNameResolver implements BeanNameResolver {

    private final ProjectManager projectManager;
    private final JavaCompiler javaCompiler;

    @Autowired
    public JavaBeanNameResolver(ProjectManager projectManager) {
        this.projectManager = projectManager;
        this.javaCompiler = ToolProvider.getSystemJavaCompiler();
    }

    @Override
    public SortedSet<String> beanNames(Path javaFile, Type type) {
        final ProjectProfile profile = projectManager.getProfile(javaFile).orElse(null);
        if (profile == null) {
            return Collections.emptySortedSet();
        }

        final StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(this::report, ENGLISH, UTF_8);

        try {
            final Path lib = profile.getTarget().resolve("lib");
            final SortedSet<File> jars = jars(lib).stream().map(Path::toFile).collect(toCollection(TreeSet::new));
            final Path srcDir = Files.createTempDirectory("src");
            final Path classesDir = Files.createTempDirectory("target");
            try {
                fileManager.setLocation(SOURCE_PATH, singletonList(srcDir.toFile()));
                fileManager.setLocation(CLASS_OUTPUT, singletonList(classesDir.toFile()));
                fileManager.setLocation(CLASS_PATH, jars);

                Files.write(srcDir.resolve("InjectedClass.java"), classCode(type).getBytes(UTF_8));
                FileUtils.copyDirectory(profile.getSrcMainJava().toFile(), srcDir.toFile());

                final Iterable<? extends JavaFileObject> objects = fileManager.list(SOURCE_PATH, "", of(SOURCE), true);

                if (!javaCompiler.getTask(null, fileManager, this::report, null, null, objects).call()) {
                    log(WARNING, "Unable to compile {0}", objects);
                    return Collections.emptySortedSet();
                }

                profile.save();

                final Set<URL> classPathUrls = Urls.classpath(lib, classesDir);
                try (final URLClassLoader classLoader = new URLClassLoader(classPathUrls.toArray(new URL[0]))) {

                    final Class<?> injectedClass = classLoader.loadClass("InjectedClass");
                    final Field field = injectedClass.getField("field");
                    final ResolvableType expectedType = ResolvableType.forField(field);

                    final TreeSet<String> names = new TreeSet<>();
                    try (final Scanner scanner = new Scanner(profile.getBeanClassesFile(), "UTF-8")) {
                        while (scanner.hasNextLine()) {
                            final String line = scanner.nextLine().trim();
                            if (line.isEmpty()) {
                                continue;
                            }
                            final Class<?> c = Class.forName(line, false, classLoader);
                            if (expectedType.isAssignableFrom(c)) {
                                names.add(Annotations.beanName(c));
                            }
                            for (final Method method : c.getMethods()) {
                                final ResolvableType t = ResolvableType.forMethodReturnType(method, c);
                                if (expectedType.isAssignableFrom(t)) {
                                    final String[] aliases = Annotations.beanName(method);
                                    if (aliases != null) {
                                        Collections.addAll(names, aliases);
                                    }
                                }
                            }
                        }
                    }
                    return names.isEmpty() ? Collections.emptySortedSet() : names;
                }
            } finally {
                MoreFiles.deleteRecursively(srcDir);
                MoreFiles.deleteRecursively(classesDir);
            }
        } catch (Exception x) {
            log(WARNING, "Unable to compile files", x);
        }

        return Collections.emptySortedSet();
    }

    private String classCode(Type type) {
        final StringBuilder builder = new StringBuilder();
        try (final Formatter formatter = new Formatter(builder)) {
            formatter.format("public class InjectedClass {%n");
            formatter.format("  public %s field;%n", type.asString());
            formatter.format("}%n");
        }
        return builder.toString();
    }

    private void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        final Level level;
        switch (diagnostic.getKind()) {
            case ERROR:
                level = SEVERE;
                break;
            case WARNING:
            case MANDATORY_WARNING:
                level = WARNING;
                break;
            case NOTE:
                level = INFO;
                break;
            default:
                level = CONFIG;
                break;
        }
        log(level, "{0} ({1}:{2}) {3}",
                diagnostic.getCode(),
                diagnostic.getLineNumber(),
                diagnostic.getColumnNumber(),
                diagnostic.getMessage(Locale.getDefault()));
    }
}
