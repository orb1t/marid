/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License 
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.marid.site;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Currency;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

/**
 * @author Dmitry Ovchinnikov
 */
@ManagedBean
@ApplicationScoped
public class CurrencyConverterBean implements Serializable, Comparator<Currency> {
    
    private static final Logger LOG = Logger.getLogger(CurrencyConverterBean.class.getName());
    
    private final Set<String> currencies = new LinkedHashSet<>(Arrays.asList("USD", "EUR", "GBP", "CHF", "RUR"));

    @Override
    public int compare(Currency o1, Currency o2) {
        return o1.getCurrencyCode().compareTo(o2.getCurrencyCode());
    }

    public Set<String> getCurrencies() {
        return currencies;
    }
    
    public float convertTo(float amount, String currency) {
        try {
            final URL url = new URL(new StringBuilder("http://www.google.com/ig/calculator?hl=en&q=")
                    .append(amount)
                    .append("USD")
                    .append("%3D%3F")
                    .append(currency)
                    .toString());
            final StringBuilder responseBuilder = new StringBuilder();
            try (final InputStreamReader r = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8)) {
                final char[] buf = new char[128];
                while (true) {
                    final int n = r.read(buf);
                    if (n < 0) {
                        break;
                    }
                    responseBuilder.append(buf, 0, n);
                }
            }
            final JSONObject json = new JSONObject(responseBuilder.toString());
            final String value = json.getString("rhs").split("\\s+")[0];
            return Float.parseFloat(value.replaceAll("[^\\d.]", ""));
        } catch (IOException | JSONException x) {
            LOG.log(Level.WARNING, "Unable to convert the currency", x);
            return amount;
        }
    }
}
