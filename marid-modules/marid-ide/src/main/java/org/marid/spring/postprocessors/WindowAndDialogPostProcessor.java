package org.marid.spring.postprocessors;

import javafx.scene.control.Dialog;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Dmitry Ovchinnikov
 */
public class WindowAndDialogPostProcessor implements BeanPostProcessor {

    private final List<Dialog<?>> dialogs = new ArrayList<>();
    private final List<Window> windows = new ArrayList<>();
    private final AnnotationConfigApplicationContext context;

    public WindowAndDialogPostProcessor(AnnotationConfigApplicationContext context) {
        this.context = context;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (beanName != null && matches(bean) && context.isSingleton(beanName)) {
            if (bean instanceof Dialog<?>) {
                final Dialog<?> dialog = (Dialog<?>) bean;
                dialog.showingProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue) {
                        dialogs.remove(dialog);
                        closeIfNecessary();
                    }
                });
                dialogs.add(dialog);
            } else if (bean instanceof Window) {
                final Window window = (Window) bean;
                window.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> {
                    windows.remove(window);
                    closeIfNecessary();
                });
                windows.add(window);
            }
        }
        return bean;
    }

    private static boolean matches(Object bean) {
        return bean instanceof Dialog || bean instanceof Window;
    }

    private void closeIfNecessary() {
        if (dialogs.isEmpty() && windows.isEmpty()) {
            context.close();
        }
    }
}
