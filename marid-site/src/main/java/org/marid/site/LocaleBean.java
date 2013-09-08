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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import static javax.faces.context.FacesContext.getCurrentInstance;

/**
 * @author Dmitry Ovchinnikov
 */
@ManagedBean
@SessionScoped
public class LocaleBean implements Serializable {

    private static final Logger LOG = Logger.getLogger(LocaleBean.class.getName());
    private Locale locale = getCurrentInstance().getViewRoot().getLocale();

    public Locale getLocale() {
        return locale;
    }

    public void selectSpanishLocale() {
        setLocale(new Locale("es"));
    }

    public void selectEnglishLocale() {
        setLocale(Locale.US);
    }
    
    public String getLanguage() {
        return locale.getLanguage().toLowerCase();
    }
    
    public void setLocale(Locale locale) {
        this.locale = locale;
        FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    }
    
    public String msg(String key) {
        return m(key);
    }

    public String msg(String key, Object v1) {
        return m(key, new Object[]{v1});
    }

    public String msg(String key, Object v1, Object v2) {
        return m(key, new Object[]{v1, v2});
    }

    public String msg(String key, Object v1, Object v2, Object v3) {
        return m(key, new Object[]{v1, v2, v3});
    }

    private String m(String key, Object... args) {
        final ResourceBundle bundle = ResourceBundle.getBundle(
                FacesContext.getCurrentInstance().getApplication().getMessageBundle(),
                locale);
        final String value = bundle.containsKey(key) ? bundle.getString(key) : key;
        try {
            return MessageFormat.format(value, args);
        } catch (Exception x) {
            final LogRecord r = new LogRecord(Level.WARNING, "Unable to format '{0}' with {1}");
            r.setParameters(new Object[]{value, Arrays.deepToString(args)});
            r.setThrown(x);
            LOG.log(r);
            return key;
        }
    }
}
