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

package org.marid.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.printer.PrettyPrinter;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.marid.ide.event.TextFileChangedEvent;
import org.marid.ide.event.TextFileMovedEvent;
import org.marid.ide.model.TextFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class JavaFileHolder {

	private final TextFile javaFile;
	private final PrettyPrinter prettyPrinter;
	private final ObjectProperty<CompilationUnit> compilationUnit = new SimpleObjectProperty<>();
	private final ObjectProperty<ClassOrInterfaceDeclaration> type = new SimpleObjectProperty<>();

	@Autowired
	public JavaFileHolder(TextFile javaFile, PrettyPrinter prettyPrinter) {
		this.javaFile = javaFile;
		this.prettyPrinter = prettyPrinter;
	}

	public ObjectProperty<CompilationUnit> compilationUnitProperty() {
		return compilationUnit;
	}

	public CompilationUnit getCompilationUnit() {
		return compilationUnit.get();
	}

	public ClassOrInterfaceDeclaration getType() {
		return type.get();
	}

	@PostConstruct
	public void update() {
		try {
			compilationUnit.set(JavaParser.parse(javaFile.getPath(), UTF_8));
			type.set(compilationUnit.get().getTypes().stream()
					.filter(ClassOrInterfaceDeclaration.class::isInstance)
					.map(ClassOrInterfaceDeclaration.class::cast)
					.findFirst()
					.orElse(null));
			log(INFO, "Updated {0}", javaFile);
		} catch (Exception x) {
			log(WARNING, "Unable to parse {0}", x, javaFile);
		}
	}

	public void save() {
		try {
			Files.write(javaFile.getPath(), prettyPrinter.print(getCompilationUnit()).getBytes(UTF_8));
		} catch (IOException x) {
			log(WARNING, "Unable to save {0}", x, javaFile);
		}
	}

	@EventListener(condition = "@javaFile.path.equals(#event.source)")
	public void onMove(TextFileMovedEvent event) {
		Platform.runLater(() -> {
			javaFile.setPath(event.getTarget());
			update();
		});
	}

	@EventListener(condition = "@javaFile.path.equals(#event.source)")
	public void onChange(TextFileChangedEvent event) {
		Platform.runLater(this::update);
	}
}
