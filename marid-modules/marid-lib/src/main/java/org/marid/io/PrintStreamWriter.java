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

package org.marid.io;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Locale;

/**
 * @author Dmitry Ovchinnikov
 */
public class PrintStreamWriter extends PrintWriter {

    private final PrintStream printStream;

    public PrintStreamWriter(PrintStream printStream, boolean autoFlush) {
        super(printStream, autoFlush);
        this.printStream = printStream;
    }

    @Override
    public void write(char[] buf) {
        printStream.print(buf);
    }

    @Override
    public void write(char[] buf, int off, int len) {
        printStream.print(new String(buf, off, len));
    }

    @Override
    public void write(int c) {
        printStream.print((char) c);
    }

    @Override
    public void write(String s) {
        printStream.print(s);
    }

    @Override
    public void write(String s, int off, int len) {
        printStream.print(s.substring(off, off + len));
    }

    @Override
    public PrintWriter printf(String format, Object... args) {
        printStream.printf(format, args);
        return this;
    }

    @Override
    public PrintWriter printf(Locale l, String format, Object... args) {
        printStream.printf(l, format, args);
        return this;
    }

    @Override
    public void print(boolean b) {
        printStream.print(b);
    }

    @Override
    public void print(char c) {
        printStream.print(c);
    }

    @Override
    public void print(double d) {
        printStream.print(d);
    }

    @Override
    public void print(float f) {
        printStream.print(f);
    }

    @Override
    public void print(int i) {
        printStream.print(i);
    }

    @Override
    public void print(long l) {
        printStream.print(l);
    }

    @Override
    public void print(Object obj) {
        printStream.print(obj);
    }

    @Override
    public void print(char[] s) {
        printStream.print(s);
    }

    @Override
    public void print(String s) {
        printStream.print(s);
    }

    @Override
    public void println() {
        printStream.println();
    }

    @Override
    public void println(boolean x) {
        printStream.println(x);
    }

    @Override
    public void println(char x) {
        printStream.println(x);
    }

    @Override
    public void println(int x) {
        printStream.println(x);
    }

    @Override
    public void println(long x) {
        printStream.println(x);
    }

    @Override
    public void println(float x) {
        printStream.println(x);
    }

    @Override
    public void println(double x) {
        printStream.println(x);
    }

    @Override
    public void println(char[] x) {
        printStream.println(x);
    }

    @Override
    public void println(String x) {
        printStream.println(x);
    }

    @Override
    public void println(Object x) {
        printStream.println(x);
    }

    @Override
    public PrintWriter format(String format, Object... args) {
        printStream.format(format, args);
        return this;
    }

    @Override
    public PrintWriter format(Locale l, String format, Object... args) {
        printStream.format(l, format, args);
        return this;
    }

    @Override
    public void flush() {
        printStream.flush();
    }

    @Override
    public void close() {
        printStream.close();
    }

    @Override
    public boolean checkError() {
        return printStream.checkError();
    }
}
