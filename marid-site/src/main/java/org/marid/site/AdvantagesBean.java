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
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.marid.site.model.Advantage;

/**
 * @author Dmitry Ovchinnikov
 */
@ManagedBean
@SessionScoped
public class AdvantagesBean implements Serializable {
    
    @ManagedProperty("#{rb}")
    private ResourcesBean rb;

    public void setRb(ResourcesBean rb) {
        this.rb = rb;
    }
    
    public Advantage[] getAdvantages() {
        return Advantage.values();
    }
    
    public String label(Advantage advantage) {
        return rb.getBundle().containsKey(advantage.name()) ?
                rb.getBundle().getString(advantage.name()) : advantage.getText();
    }
}
