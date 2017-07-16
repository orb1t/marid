package org.marid.runtime.beans;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public class LogBeanListener implements BeanListener {

    @Override
    public void onEvent(BeanEvent event) {
        log(INFO, "{0}", event);
    }
}
