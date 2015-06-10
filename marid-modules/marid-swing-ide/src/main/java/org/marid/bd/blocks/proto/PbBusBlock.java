/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
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

package org.marid.bd.blocks.proto;

import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.marid.bd.BlockColors;
import org.marid.bd.StandardBlock;
import org.marid.bd.blocks.BdBlock;
import org.marid.xml.bind.adapter.MapExpressionXmlAdapter;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.EventListener;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * @author Dmitry Ovchinnikov
 */
@BdBlock(name = "Proto Bus", iconText = "PBus", color = BlockColors.RED)
@XmlRootElement
public class PbBusBlock extends StandardBlock {

    @XmlAttribute
    String busName = "bus0";

    @XmlElement
    @XmlJavaTypeAdapter(MapExpressionXmlAdapter.class)
    final MapExpression map = new MapExpression();

    public final Out out = new Out("bus", MapEntryExpression.class, () -> new MapEntryExpression(new ConstantExpression(busName), map));

    public void setBusName(String busName) {
        if (!Objects.equals(busName, this.busName)) {
            this.busName = busName;
            fireEvent(PbBusBlockListener.class, l -> l.busNameChanged(busName));
        }
    }

    public void changeMap(Consumer<MapExpression> mapExpressionConsumer) {
        mapExpressionConsumer.accept(map);
        fireEvent(PbBusBlockListener.class, l -> l.mapChanged(map));
    }

    public interface PbBusBlockListener extends EventListener {

        void busNameChanged(String name);

        void mapChanged(MapExpression map);
    }
}
