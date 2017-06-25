package org.marid.l10n;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Dmitry Ovchinnikov
 */
public class ChainedPropertyResourceBundle extends ResourceBundle {

    private final ArrayList<Properties> propertiesList = new ArrayList<>();

    public void load(URL url, boolean useCaches) throws IOException {
        final URLConnection connection = url.openConnection();
        connection.setUseCaches(useCaches);
        try (final Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            final Properties properties = new Properties();
            properties.load(reader);
            propertiesList.add(properties);
        }
        propertiesList.trimToSize();
    }

    @Override
    public boolean containsKey(@Nonnull String key) {
        return propertiesList.stream().anyMatch(p -> p.containsKey(key)) || parent != null && parent.containsKey(key);
    }

    @Override
    protected String handleGetObject(@Nonnull String key) {
        return propertiesList.stream()
                .map(p -> p.getProperty(key))
                .filter(Objects::nonNull)
                .findAny()
                .orElse(null);
    }

    @Nonnull
    @Override
    protected Set<String> handleKeySet() {
        return propertiesList.stream().flatMap(p -> p.stringPropertyNames().stream()).collect(Collectors.toSet());
    }

    @Nonnull
    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(keySet());
    }
}
