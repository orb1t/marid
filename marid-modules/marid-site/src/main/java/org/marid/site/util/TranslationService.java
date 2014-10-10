/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.site.util;

import org.eclipse.rap.json.JsonObject;
import org.eclipse.rap.rwt.RWT;
import org.marid.concurrent.MaridTimerTask;
import org.marid.logging.LogSupport;

import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.prefs.Preferences;

/**
 * @author Dmitry Ovchinnikov
 */
public class TranslationService implements LogSupport {

    private static final int QUEUE_SIZE = 4096;
    private static final long CHECK_PERIOD = 10_000L;
    private static final Preferences PREFERENCES = Preferences.userRoot().node("TRANSLATION");
    private static final ArrayBlockingQueue<String[]> QUEUE = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private static final Timer TIMER = new Timer(true);

    static {
        TIMER.schedule(new MaridTimerTask(() -> {
            final ArrayList<String[]> queue = new ArrayList<>(QUEUE_SIZE);
            QUEUE.drainTo(queue);
            for (final String[] pair : queue) {
                if (pair == null || pair.length < 2) {
                    continue;
                }
                try {
                    final URL url = new URL("http://mymemory.translated.net/api/get?q=" +
                            URLEncoder.encode(pair[1], "UTF-8") + '&' +
                            "langpair=" + "en|" + pair[0] + '&' +
                            "de=" + "d.ovchinnikow@gmail.com");
                    try (final Reader reader = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                        final JsonObject result = JsonObject.readFrom(reader);
                        final int responseStatus = result.get("responseStatus").asInt();
                        if (responseStatus == 200) {
                            final JsonObject responseData = result.get("responseData").asObject();
                            final String translatedText = responseData.get("translatedText").asString();
                            PREFERENCES.node(pair[0]).put(pair[1], translatedText);
                        }
                    }
                } catch (Exception x) {
                    Log.warning("Translation error for {0}", x, Arrays.asList(pair));
                }
            }
        }), CHECK_PERIOD, CHECK_PERIOD);
    }

    public static String translate(String text, Locale locale) {
        final String lang = locale.getLanguage();
        if ("en".equals(lang)) {
            return text;
        }
        final Preferences node = PREFERENCES.node(lang);
        final String translation = node.get(text, null);
        if (translation == null) {
            try {
                QUEUE.add(new String[]{lang, text});
            } catch (IllegalStateException x) {
                // ignored
            }
            return text;
        } else {
            return translation;
        }
    }

    public static String translate(String text) {
        return translate(text, RWT.getLocale());
    }
}
