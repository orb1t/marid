/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.dependant.modbus.codec;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.marid.jfx.beans.FxList;
import org.marid.logging.LogSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.function.DoubleFunction;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;

import static java.util.stream.IntStream.range;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.9
 */
@Repository
public class CodecManager implements LogSupport {

    private final ExpressionParser parser = new SpelExpressionParser();
    private final FxList<Pair<String, DoubleFunction<byte[]>>> codecs = new FxList<>();
    private final Preferences preferences;

    @Autowired
    public CodecManager(Preferences preferences) {
        this.preferences = preferences;
    }

    @PostConstruct
    private void initStdCodecs() {
        codecs.add(new Pair<>("Float number", v -> Ints.toByteArray(Float.floatToIntBits((float) v))));
        codecs.add(new Pair<>("Double number", v -> Longs.toByteArray(Double.doubleToLongBits(v))));
        codecs.add(new Pair<>("Integer number", v -> Ints.toByteArray((int) v)));
    }

    @PostConstruct
    private void initCustomCodecs() throws BackingStoreException {
        for (final String key : preferences.keys()) {
            final String expr = preferences.get(key, null);
            if (expr == null || expr.isEmpty()) {
                preferences.remove(key);
            }
            compile(key, expr);
        }
    }

    @PostConstruct
    private void registerPrefListener() {
        preferences.addPreferenceChangeListener(this::onPreferenceChange);
    }

    @PreDestroy
    private void unregisterPrefListener() {
        preferences.removePreferenceChangeListener(this::onPreferenceChange);
    }

    private synchronized void compile(String key, String expr) {
        if (expr == null) {
            codecs.removeIf(p -> key.equals(p.getKey()));
            return;
        }
        try {
            final Expression expression = parser.parseExpression(expr);
            final Pair<String, DoubleFunction<byte[]>> codec = new Pair<>(key, v -> {
                final EvaluationContext context = new StandardEvaluationContext();
                context.setVariable("v", v);
                return expression.getValue(context, byte[].class);
            });
            final int n = range(0, codecs.size()).filter(i -> codecs.get(i).getKey().equals(key)).findAny().orElse(-1);
            if (n < 0) {
                codecs.add(codec);
            } else {
                codecs.set(n, codec);
            }
        } catch (ParseException e) {
            log(WARNING, "Unable to compile {0}:{1}", e, key, expr);
        }
    }

    private void onPreferenceChange(PreferenceChangeEvent event) {
        compile(event.getKey(), event.getNewValue());
    }

    public ObservableList<Pair<String, DoubleFunction<byte[]>>> getCodecs() {
        return codecs;
    }
}
