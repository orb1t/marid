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

package org.marid.beans;

import org.codehaus.groovy.ast.expr.ConstantExpression;

import java.beans.PersistenceDelegate;
import java.util.function.BiConsumer;

import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;

/**
 * @author Dmitry Ovchinnikov
 */
public class GroovyPersistenceDelegates implements MaridBeans {

    @Override
    public void visitPersistenceDelegates(BiConsumer<Class<?>, PersistenceDelegate> consumer) {
        consumer.accept(ConstantExpression.class, new ConstructorDelegate<ConstantExpression>(
                (c, e) -> new Object[]{c.getValue(), isPrimitiveType(c.getType())}));
    }
}
