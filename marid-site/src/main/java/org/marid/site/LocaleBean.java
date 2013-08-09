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
import java.util.Locale;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import static javax.faces.context.FacesContext.getCurrentInstance;

/**
 * @author Dmitry Ovchinnikov
 */
@ManagedBean
@SessionScoped
public class LocaleBean implements Serializable {

    private Locale locale = getCurrentInstance().getViewRoot().getLocale();

    public Locale getLocale() {
        return locale;
    }

    public void selectSpanishLocale() {
        locale = new Locale("es");
    }

    public void selectEnglishLocale() {
        locale = Locale.ENGLISH;
    }
    
    public String getLanguage() {
        return locale.getLanguage().toLowerCase();
    }
}
