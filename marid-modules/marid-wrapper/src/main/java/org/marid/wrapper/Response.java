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

package org.marid.wrapper;

/**
 * @author Dmitry Ovchinnikov
 */
public class Response {

    public static final byte ACCESS_DENIED = -128;
    public static final byte WAIT_LOCK_FAILED = -127;
    public static final byte UNKNOWN_REQUEST = -126;
    public static final byte OK = 0;
    public static final byte WAIT_LOCK = 1;
    public static final byte BREAK = 2;
    public static final byte EXCEPTION = 3;
    public static final byte LOG_RECORDS = 4;
    public static final byte WAIT_DATA = 5;
}
