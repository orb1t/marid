package org.marid.dependant.settings;

import org.marid.ide.settings.MavenSettings;
import org.marid.jfx.panes.GenericGridPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class MavenTab extends GenericGridPane implements SettingsEditor {

    private final MavenSettings pref;

    @Autowired
    public MavenTab(MavenSettings pref) {
        this.pref = pref;
        addTextField("Releases update policy by default", pref.releaseUpdatePolicy);
        addTextField("Snapshot update policy by default", pref.snapshotUpdatePolicy);
        addSeparator();
        addTextField("Dependency plugin version", pref.dependencyPluginVersion);
        addTextField("Compiler plugin version", pref.compilerPluginVersion);
        addTextField("JAR plugin version", pref.jarPluginVersion);
        addTextField("Resources plugin version", pref.resourcesPluginVersion);
    }

    @Override
    public MavenSettings getSettings() {
        return pref;
    }
}
