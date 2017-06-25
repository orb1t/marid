package org.marid.ide.common;

import javafx.application.Platform;
import org.marid.IdePrefs;
import org.marid.jfx.LocalizedStrings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Locale;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Lazy(false)
public class IdeValues {

    public final String implementationVersion;

    @Autowired
    public IdeValues(@Value("${implementation.version}") String implementationVersion) {
        this.implementationVersion = implementationVersion;
    }

    @PostConstruct
    private void init() {
        IdePrefs.PREFERENCES.addPreferenceChangeListener(evt -> {
            if ("locale".equals(evt.getKey())) {
                final Locale locale = Locale.forLanguageTag(evt.getNewValue());
                if (locale != null && !Locale.ROOT.equals(locale) && !Locale.getDefault().equals(locale)) {
                    Locale.setDefault(locale);
                    if (Platform.isFxApplicationThread()) {
                        LocalizedStrings.LOCALE.set(locale);
                    } else {
                        Platform.runLater(() -> LocalizedStrings.LOCALE.set(locale));
                    }
                }
            }
        });
    }
}
