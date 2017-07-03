package org.marid.dependant.beaneditor.beans;

import com.github.javaparser.ast.body.MethodDeclaration;
import org.marid.dependant.beaneditor.model.BeanFactoryMethod;
import org.marid.jfx.action.FxAction;

import java.util.function.Function;

/**
 * @author Dmitry Ovchinnikov
 */
public interface MethodActionSupplier extends Function<BeanFactoryMethod, FxAction> {

    @Override
    default FxAction apply(BeanFactoryMethod method) {
        return apply(method, method == null ? null : method.method.get());
    }

    FxAction apply(BeanFactoryMethod bfm, MethodDeclaration md);
}
