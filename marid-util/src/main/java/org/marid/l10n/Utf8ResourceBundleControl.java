package org.marid.l10n;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Dmitry Ovchinnikov
 */
public class Utf8ResourceBundleControl extends ResourceBundle.Control {

    public static final Utf8ResourceBundleControl UTF8CTRL = new Utf8ResourceBundleControl();

    private Utf8ResourceBundleControl() {
    }

    @Override
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader classLoader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
        final ChainedPropertyResourceBundle bundle = new ChainedPropertyResourceBundle();
        final String resource = toResourceName(toBundleName(baseName, locale), "properties");
        for (final Enumeration<URL> e = classLoader.getResources(resource); e.hasMoreElements(); ) {
            bundle.load(e.nextElement(), !reload);
        }
        return bundle;
    }

    @Override
    public List<String> getFormats(String baseName) {
        return FORMAT_PROPERTIES;
    }
}
