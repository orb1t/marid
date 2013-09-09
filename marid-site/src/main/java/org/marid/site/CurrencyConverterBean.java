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

import com.tunyk.currencyconverter.BankUaCom;
import com.tunyk.currencyconverter.api.Currency;
import com.tunyk.currencyconverter.api.CurrencyConverter;
import com.tunyk.currencyconverter.api.CurrencyConverterException;
import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dmitry Ovchinnikov
 */
@ManagedBean
@ApplicationScoped
public class CurrencyConverterBean implements Serializable {
    
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private CurrencyConverter currencyConverter;
    
    @PostConstruct
    public void init() {
        try {
            currencyConverter = new BankUaCom(Currency.USD, Currency.EUR);
        } catch (CurrencyConverterException x) {
            logger.warn("Currency converter exception", x);
        }
    }
    
    public float convertTo(float amount, Currency currency) {
        if (currencyConverter != null) {
            try {
                return currencyConverter.convertCurrency(amount, currency);
            } catch (CurrencyConverterException x) {
                logger.warn("Currency converter exception", x);
                return amount;
            }
        } else {
            return amount;
        }
    }
}
