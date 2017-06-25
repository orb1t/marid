package org.marid;

import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdePrefs {

    public static final Preferences PREFERENCES = Preferences.userNodeForPackage(IdePrefs.class).node("Ide");
}
