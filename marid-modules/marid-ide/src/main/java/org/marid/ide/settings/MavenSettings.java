package org.marid.ide.settings;

import javafx.beans.value.WritableObjectValue;
import org.springframework.stereotype.Component;

import static org.marid.jfx.props.Props.string;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class MavenSettings extends AbstractSettings {

    public final WritableObjectValue<String> snapshotUpdatePolicy = string(preferences, "snapshotUpdatePolicy", null);
    public final WritableObjectValue<String> releaseUpdatePolicy = string(preferences, "releaseUpdatePolicy", null);
    public final WritableObjectValue<String> dependencyPluginVersion = string(preferences, "dependencyPluginVersion", "3.0.0");
    public final WritableObjectValue<String> compilerPluginVersion = string(preferences, "compilerPluginVersion", "3.6.1");
    public final WritableObjectValue<String> jarPluginVersion = string(preferences, "jarPluginVersion", "3.0.2");
    public final WritableObjectValue<String> resourcesPluginVersion = string(preferences, "resourcesPluginVersion", "3.0.2");

    @Override
    public String getName() {
        return "Maven";
    }
}
