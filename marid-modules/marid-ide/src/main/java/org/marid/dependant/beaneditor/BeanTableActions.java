package org.marid.dependant.beaneditor;

import org.marid.jfx.action.FxAction;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanTableActions {

    Map<String, FxAction> tableActions() {
        final Map<String, FxAction> actionMap = new HashMap<>();
        return actionMap;
    }
}
