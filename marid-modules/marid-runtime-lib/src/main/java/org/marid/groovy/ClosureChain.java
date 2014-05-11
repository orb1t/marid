/*
 * Copyright (C) 2013 Dmitry Ovchinnikov
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

package org.marid.groovy;

import groovy.lang.Closure;
import org.marid.func.BreakException;
import org.marid.func.SkipException;
import org.marid.tree.TreeObject;

import java.util.Collection;
import java.util.logging.Logger;

import static org.marid.methods.LogMethods.fine;
import static org.marid.methods.LogMethods.warning;

/**
 * @author Dmitry Ovchinnikov
 */
public class ClosureChain {

    private final Collection<? extends Closure> closures;

    public ClosureChain(Collection<? extends Closure> closures) {
        this.closures = closures;
    }

    public boolean isEmpty() {
        return closures.isEmpty();
    }

    public Object call(Logger logger, TreeObject object, Object result) {
        for (final Closure closure : closures) {
            try {
                result = closure.call(object, result);
            } catch (SkipException sx) {
                if (sx.getMessage() != null) {
                    if (sx.getCause() != null) {
                        warning(logger, sx.getMessage(), sx.getCause(), sx.getArgs());
                    } else {
                        warning(logger, sx.getMessage(), sx.getArgs());
                    }
                } else {
                    if (sx.getCause() != null) {
                        warning(logger, "Skipped", sx.getCause());
                    } else {
                        fine(logger, "Skipped");
                    }
                }
            } catch (BreakException bx) {
                if (bx.getMessage() != null) {
                    if (bx.getCause() != null) {
                        warning(logger, bx.getMessage(), bx.getCause(), bx.getArgs());
                    } else {
                        warning(logger, bx.getMessage(), bx.getArgs());
                    }
                } else {
                    if (bx.getCause() != null) {
                        warning(logger, "Break", bx.getCause());
                    } else {
                        fine(logger, "Break");
                    }
                }
                break;
            }
        }
        return result;
    }
}
