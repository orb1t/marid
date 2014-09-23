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

import org.marid.logging.LogSupport;
import org.primefaces.json.JSONException;
import org.primefaces.json.JSONObject;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.*;

/**
 * @author Dmitry Ovchinnikov
 */
@ManagedBean
@ApplicationScoped
public class CurrencyConverterBean implements Serializable, Comparator<Currency>, LogSupport {

    private static final String RATE_EXCHANGE_SITE = "http://rate-exchange.appspot.com/currency";

    private final Set<String> currencies = new LinkedHashSet<>(Arrays.asList("USD", "EUR", "GBP", "CHF"));

    @Override
    public int compare(Currency o1, Currency o2) {
        return o1.getCurrencyCode().compareTo(o2.getCurrencyCode());
    }

    public Set<String> getCurrencies() {
        return currencies;
    }
    
    public float convertTo(float amount, String currency) {
        try {
            final URL url = new URL(RATE_EXCHANGE_SITE + "?from=USD&to=" + currency);
            final JSONObject json;
            try (final Scanner scanner = new Scanner(url.openStream()).useDelimiter("\\z")) {
                json = new JSONObject(scanner.next());
            }
            final double rate = json.getDouble("rate");
            return (float) (rate * amount);
        } catch (IOException | JSONException x) {
            warning("Unable to convert the currency", x);
            return amount;
        }
    }
}
