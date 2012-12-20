/*
 * Copyright (C) 2012 Dmitry Ovchinnikov
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
package org.marid.util;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Date and calendar utilities.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class DateUtil {
	/**
	 * Parses an ISO-formatted date.
	 * @param date ISO-formatted date.
	 * Possible formats are:
	 * <ol>
	 *	<li>yyyy-MM-dd'T'HH:mm:ss.SSSZ (28 characters)</li>
	 *  <li>yyyy-MM-dd'T'HH:mm:ss.SSS z (27 characters)</li>
	 *  <li>yyyy-MM-dd'T'HH:mm:ss.SSS (23 characters)</li>
	 *	<li>yyyy-MM-dd'T'HH:mm.ssZ (25 characters)</li>
	 *  <li>yyyy-MM-dd'T'HH:mm:ss z (24 characters)</li>
	 *	<li>yyyy-MM-dd'T'HH:mm:ss (20 characters)</li>
	 *  <li>yyyy-MM-dd'T'HH:mmZ (21 characters)</li>
	 *  <li>yyyy-MM-dd'T'HH:mm z (20 characters)</li>
	 *  <li>yyyy-MM-dd'T'HH:mm (16 characters)</li>
	 *  <li>yyyy-MM-dd'T'HHZ (18 characters)</li>
	 *  <li>yyyy-MM-dd'T'HH z (17 characters)</li>
	 *	<li>yyyy-MM-dd'T'HH (13 characters)</li>
	 *	<li>yyyy-MM-ddZ (15 characters)</li>
	 *  <li>yyyy-MM-dd z (14 characters)</li>
	 *  <li>yyyy-MM-dd (10 characters)</li>
	 *  <li>HH:mm:ss.SSS (12 characters)</li>
	 *  <li>HH:mm:ss (8 characters)</li>
	 *  <li>HH:mm (5 characters)</li>
	 * </ol>
	 * @return Calendar.
	 */
	public static Calendar isoToCalendar(String date) {
		TimeZone z;
		int l = date.length();
		switch (l) {
			case 28:
				z = TimeZone.getTimeZone("GMT" + date.substring(23, 26) +
						":" + date.substring(26, 28));
				break;
			case 27:
				z = TimeZone.getTimeZone(date.substring(24, 27));
				break;
			case 25:
				z = TimeZone.getTimeZone("GMT" + date.substring(20, 23) +
						":" + date.substring(23, 25));
				break;
			case 24:
				z = TimeZone.getTimeZone(date.substring(21, 24));
				break;
			case 21:
				z = TimeZone.getTimeZone("GMT" + date.substring(16, 19) +
						":" + date.substring(19, 21));
				break;
			default:
				z = TimeZone.getDefault();
				break;
		}
		Calendar c = new GregorianCalendar(z, Locale.ENGLISH);
		return c;
	}

	/**
	 * Parses an ISO-formatted date.
	 * @param date ISO-formatted date.
	 * @return Milliseconds since 1970-01-01.
	 */
	public static long isoToMillis(String date) {
		return isoToCalendar(date).getTimeInMillis();
	}

	/**
	 * Parses an ISO-formatted date.
	 * @param date ISO-formatted date.
	 * @return Date object.
	 */
	public static Date isoToDate(String date) {
		return isoToCalendar(date).getTime();
	}

	/**
	 * Parses an ISO-formatted date.
	 * @param date ISO-formatted date.
	 * @return SQL timestamp.
	 */
	public static Timestamp isoToTimestamp(String date) {
		return new Timestamp(isoToMillis(date));
	}
}
