package org.marid.jfx;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import org.marid.jfx.beans.OProp;

import java.util.Locale;
import java.util.stream.Stream;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
public class LocalizedStrings {

    public static final OProp<Locale> LOCALE = new OProp<>("locale", Locale.getDefault());

    public static ObservableValue<String> ls(String text, Object... args) {
        return Bindings.createStringBinding(() -> s(LOCALE.get(), text, args), LOCALE);
    }

    public static ObservableValue<String> fls(String format, String text, Object... args) {
        return Bindings.createStringBinding(() -> String.format(format, s(LOCALE.get(), text, args)), LOCALE);
    }

    public static ObservableValue<String> fs(String text, Object... args) {
        final Observable[] observables = Stream.concat(
                Stream.of(LOCALE),
                Stream.of(args).filter(Observable.class::isInstance).map(Observable.class::cast)
        ).toArray(Observable[]::new);
        return Bindings.createStringBinding(() -> {
            final Object[] params = Stream.of(args)
                    .map(o -> o instanceof ObservableValue<?> ? ((ObservableValue<?>) o).getValue() : o)
                    .toArray();
            return s(LOCALE.get(), text, params);
        }, observables);
    }
}
