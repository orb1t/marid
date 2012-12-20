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
package org.marid.logging;

import java.io.BufferedWriter;
import java.util.logging.ErrorManager;
import static java.util.logging.ErrorManager.*;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;

/**
 * Fast file handler.
 * @author Dmitry Ovchinnikov (d.ovchinnikow at gmail.com)
 */
public class FastFileHandler extends Handler {
	
	private BufferedWriter writer;
	private final String pattern;
	private int genericFailureCount;
	private int writeFailureCount;
	private int flushFailureCount;
	private int closeFailureCount;
	private int openFailureCount;
	private int formatFailureCount;
	
	/**
	 * Constructs the fast file handler.
	 * @throws InstantiationException An exception when it is not possible to
	 * create an instance of formatter or filter.
	 * @throws ClassNotFoundException An exception when it is not possible to
	 * find a formattar or filter class.
	 * @throws IllegalAccessException An exception when it is not possible to
	 * acces to a formatter or filter class.
	 */
	public FastFileHandler() throws	InstantiationException,
			ClassNotFoundException,	IllegalAccessException {
		System.err.println("I'm " + getClass().getName());
		super.setErrorManager(new FastErrorManager());
		LogManager m = LogManager.getLogManager();
		String pfx = getClass().getName();
		String pat = m.getProperty(pfx + ".pattern");
		pattern = pat == null ? "%h/marid/%l/%g" : pat;
		String fmtClass = m.getProperty(pfx + ".formatter");
		if (fmtClass == null) fmtClass = FastFormatter.class.getName();
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		if (cl == null) cl = getClass().getClassLoader();
		Class<?> c = Class.forName(fmtClass, true, cl);
		Class<? extends Formatter> fc = (Class<? extends Formatter>)c;
		setFormatter(fc.newInstance());
		String filtClass = m.getProperty(pfx + ".filter");
		if (filtClass != null) {
			c = Class.forName(filtClass, true, cl);
			Class<? extends Filter> fic = (Class<? extends Filter>)c;
			super.setFilter(fic.newInstance());
		}
	}

	@Override
	public void publish(LogRecord record) {
		if (isLoggable(record)) synchronized(this) {
			
		}
	}

	@Override
	public synchronized void flush() {
		if (writer != null) try {
			writer.flush();
		} catch (Exception x) {
			getErrorManager().error("Flush error", x, FLUSH_FAILURE);
		}
	}

	@Override
	public synchronized void close() throws SecurityException {
		if (writer != null) try {
			writer.close();
		} catch (Exception x) {
			getErrorManager().error("Close error", x, CLOSE_FAILURE);
		} finally {
			writer = null;
		}
	}

	@Override
	public synchronized ErrorManager getErrorManager() {
		return super.getErrorManager();
	}

	@Override
	public synchronized void setErrorManager(ErrorManager em) {
		super.setErrorManager(em);
	}

	@Override
	public synchronized Filter getFilter() {
		return super.getFilter();
	}

	@Override
	public synchronized void setFilter(Filter f) throws SecurityException {
		super.setFilter(f);
	}

	public int getGenericFailureCount() {
		synchronized(getErrorManager()) {
			return genericFailureCount;
		}
	}

	public int getWriteFailureCount() {
		synchronized(getErrorManager()) {
			return writeFailureCount;
		}
	}

	public int getFlushFailureCount() {
		synchronized(getErrorManager()) {
			return flushFailureCount;
		}
	}

	public int getCloseFailureCount() {
		synchronized(getErrorManager()) {
			return closeFailureCount;
		}
	}

	public int getOpenFailureCount() {
		synchronized(getErrorManager()) {
			return openFailureCount;
		}
	}

	public int getFormatFailureCount() {
		synchronized(getErrorManager()) {
			return formatFailureCount;
		}
	}
	
	private class FastErrorManager extends ErrorManager {
		@Override
		public synchronized void error(String msg, Exception ex, int code) {
			switch (code) {
				case WRITE_FAILURE:
					writeFailureCount++;
					break;
				case FORMAT_FAILURE:
					formatFailureCount++;
					break;
				case FLUSH_FAILURE:
					flushFailureCount++;
					break;
				case CLOSE_FAILURE:
					closeFailureCount++;
					break;
				case OPEN_FAILURE:
					openFailureCount++;
					break;
				case GENERIC_FAILURE:
					genericFailureCount++;
					break;
			}
		}
	}
}
