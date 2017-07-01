package org.marid.dependant.beaneditor;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import org.marid.ide.model.Annotations;
import org.marid.java.JavaFileHolder;
import org.marid.jfx.action.FxAction;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTableActions {

    private final JavaFileHolder updater;

    @Autowired
    public BeanTableActions(JavaFileHolder updater) {
        this.updater = updater;
    }

    @Bean
    @Qualifier("beanTable")
    public Function<MethodDeclaration, FxAction> addActionSupplier(SpecialAction addAction) {
        return m -> new FxAction("beans", "beans", "beans")
                .setSpecialAction(addAction)
                .setEventHandler(event -> {
                    final ClassOrInterfaceDeclaration type = updater.getCompilationUnit().getTypes().stream()
                            .filter(ClassOrInterfaceDeclaration.class::isInstance)
                            .map(ClassOrInterfaceDeclaration.class::cast)
                            .findFirst()
                            .orElseThrow(IllegalStateException::new);

                    type.addMethod("bean1", Modifier.PUBLIC)
                            .setBody(new BlockStmt(NodeList.nodeList(
                                    new ReturnStmt(new NullLiteralExpr())
                            )))
                            .setType(Void.class.getName())
                            .addAnnotation(Annotations.generated("org.marid"))
                            .addAnnotation(Annotations.bean());
                    updater.save();
                })
                .bindText("Add a bean");
    }

    @Bean
    @Qualifier("beanTable")
    public Function<MethodDeclaration, FxAction> switchLazy() {
        return m -> m == null ? null : new FxAction("beans", "beans", "beans")
                .setEventHandler(event -> {
                    final Optional<AnnotationExpr> a = m.getAnnotationByClass(Lazy.class);
                    if (a.isPresent()) {
                        m.remove(a.get());
                    } else {
                        m.addAnnotation(Annotations.lazy());
                    }
                    updater.save();
                })
                .bindText("Switch lazy flag");
    }

    @Bean
    @Qualifier("beanTable")
    public Function<MethodDeclaration, Collection<FxAction>> tableActions(
            @Qualifier("beanTable") List<Function<MethodDeclaration, FxAction>> suppliers) {
        return m -> suppliers.stream()
                .map(f -> f.apply(m))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
