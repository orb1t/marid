package org.marid.dependant.settings;

import org.marid.ide.settings.JavaSettings;
import org.marid.jfx.panes.GenericGridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.marid.jfx.props.Props.value;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class JavaTab extends GenericGridPane implements SettingsEditor {

    private final JavaSettings pref;

    @Autowired
    public JavaTab(JavaSettings pref) throws Exception {
        this.pref = pref;
        addTextField("Java executable", value(pref::getJavaExecutable, pref::setJavaExecutable));
    }

    @Override
    public JavaSettings getSettings() {
        return pref;
    }
}
