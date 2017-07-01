package org.marid.java;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.printer.PrettyPrinter;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.marid.ide.event.TextFileChangedEvent;
import org.marid.ide.event.TextFileMovedEvent;
import org.marid.ide.model.TextFile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.stream.Collectors;

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

    @PostConstruct
    public void update() {
        try {
            compilationUnit.set(JavaParser.parse(javaFile.getPath(), UTF_8));
            log(INFO, "Updated {0}", javaFile);
        } catch (Exception x) {
            log(WARNING, "Unable to parse {0}", x, javaFile);
        }
    }

    public ObservableList<MethodDeclaration> getBeans() {
        return compilationUnit.get().getTypes().stream()
                .filter(TypeDeclaration::isTopLevelType)
                .filter(t -> !t.isNestedType())
                .findFirst()
                .map(t -> t.getMethods().stream()
                        .sorted(Comparator.comparing(NodeWithSimpleName::getNameAsString))
                        .collect(Collectors.toCollection(FXCollections::observableArrayList)))
                .orElseGet(FXCollections::observableArrayList);
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
