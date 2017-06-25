package org.marid.ide.settings;

import java.util.prefs.Preferences;

import static java.util.prefs.Preferences.userNodeForPackage;

/**
 * @author Dmitry Ovchinnikov
 */
public abstract class AbstractSettings {

    protected final Preferences preferences = userNodeForPackage(getClass()).node(getClass().getName()).node(getName());

    public abstract String getName();
}
