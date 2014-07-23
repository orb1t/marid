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

package org.marid.bd.shapes;

import java.awt.*;
import java.awt.event.MouseEvent;

/**
 * @author Dmitry Ovchinnikov
 */
public class LinkShapeEvent extends AWTEvent {

    public static final int MOUSE_ENTERED = 1;
    public static final int MOUSE_EXITED = 2;

    protected final Point point;
    protected final Point pointOnScreen;
    protected final Point realPoint;

    public LinkShapeEvent(LinkShape link, int id, MouseEvent sourceEvent) {
        super(link, id);
        this.point = sourceEvent.getPoint();
        this.pointOnScreen = sourceEvent.getLocationOnScreen();
        this.realPoint = point;
    }

    @Override
    public LinkShape getSource() {
        return (LinkShape) super.getSource();
    }

    public Point getPoint() {
        return point;
    }

    public Point getPointOnScreen() {
        return pointOnScreen;
    }

    public Point getRealPoint() {
        return realPoint;
    }

    public String action() {
        switch (id) {
            case MOUSE_ENTERED:
                return "entered";
            case MOUSE_EXITED:
                return "exited";
            default:
                throw new IllegalArgumentException(Integer.toString(id));
        }
    }

    @Override
    public String paramString() {
        return String.format("%s point=%s, onScreen=%s, real=%s", action(), point, pointOnScreen, realPoint);
    }
}
