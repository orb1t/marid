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
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import org.primefaces.model.mindmap.DefaultMindmapNode;
import org.primefaces.model.mindmap.MindmapNode;

/**
 * @author Dmitry Ovchinnikov
 */
@ManagedBean
@SessionScoped
public class ElectroSystemModel implements Serializable {
   
    @ManagedProperty(value = "#{rb}")
    private ResourcesBean rb;
    private MindmapNode root;
        
    @PostConstruct
    public void init() {
        root = new DefaultMindmapNode(rb.msg("Server"), "server", "04FF10", true);
        for (int c = 0; c < 6; c++) {
            final String cName = rb.msg("Controller {0}", c);
            final MindmapNode co = new DefaultMindmapNode(cName, "c" + c, "FF1034", true);
            for (int ec = 0; ec < 3; ec++) {
                final String ecName = rb.msg("Meter {0}", ec);
                final MindmapNode eco = new DefaultMindmapNode(ecName, "ec" + ec, "3410FF", false);
                co.addNode(eco);
            }
            root.addNode(co);
        }
    }

    public MindmapNode getRoot() {
        return root;
    }

    public void setRb(ResourcesBean rb) {
        this.rb = rb;
    }
}
