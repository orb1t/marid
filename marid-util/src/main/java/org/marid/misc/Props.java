package org.marid.misc;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Dmitry Ovchinnikov
 */
public interface Props {

    static void merge(Properties properties, String... resources) throws IOException {
        for (final String resource : resources) {
            try (final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource)) {
                if (inputStream != null) {
                    properties.load(inputStream);
                }
            }
        }
    }
}
