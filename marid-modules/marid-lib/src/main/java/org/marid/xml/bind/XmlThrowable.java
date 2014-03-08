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

package org.marid.xml.bind;

import org.marid.io.ThrowableWrapper;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "throwable")
public class XmlThrowable {

    @XmlAttribute
    private final String type;

    @XmlElement
    private final String message;

    @XmlElement(name = "ste")
    private final StackTraceElementWrapper[] stackTraceElements;

    @XmlElement
    private final XmlThrowable cause;

    @XmlElement
    private final XmlThrowable[] suppressed;

    public XmlThrowable() {
        type = null;
        message = null;
        stackTraceElements = null;
        cause = null;
        suppressed = null;
    }

    public XmlThrowable(Throwable throwable) {
        type = throwable.getClass().getName();
        message = throwable.getMessage();
        final StackTraceElement[] stes = throwable.getStackTrace();
        if (stes.length == 0) {
            stackTraceElements = null;
        } else {
            stackTraceElements = new StackTraceElementWrapper[stes.length];
            for (int i = 0; i < stes.length; i++) {
                stackTraceElements[i] = new StackTraceElementWrapper(stes[i]);
            }
        }
        cause = throwable.getCause() == null ? null : new XmlThrowable(throwable.getCause());
        final Throwable[] spd = throwable.getSuppressed();
        if (spd.length == 0) {
            suppressed = null;
        } else {
            suppressed = new XmlThrowable[spd.length];
            for (int i = 0; i < suppressed.length; i++) {
                suppressed[i] = new XmlThrowable(spd[i]);
            }
        }
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public XmlThrowable getCause() {
        return cause;
    }

    public XmlThrowable[] getSuppressed() {
        return suppressed;
    }

    private static ThrowableWrapper toThrowable(XmlThrowable xmlThrowable) {
        final ThrowableWrapper throwableWrapper = new ThrowableWrapper(
                xmlThrowable.type,
                xmlThrowable.message,
                xmlThrowable.cause == null ? null : toThrowable(xmlThrowable.cause));
        if (xmlThrowable.stackTraceElements != null) {
            final StackTraceElement[] stes = new StackTraceElement[xmlThrowable.stackTraceElements.length];
            for (int i = 0; i < stes.length; i++) {
                stes[i] = xmlThrowable.stackTraceElements[i].getStackTraceElement();
            }
            throwableWrapper.setStackTrace(stes);
        }
        if (xmlThrowable.suppressed != null) {
            for (final XmlThrowable xmlSupressed : xmlThrowable.suppressed) {
                throwableWrapper.addSuppressed(toThrowable(xmlSupressed));
            }
        }
        return throwableWrapper;
    }

    public ThrowableWrapper toThrowable() {
        return toThrowable(this);
    }

    @Override
    public String toString() {
        return type;
    }

    private static class StackTraceElementWrapper {

        @XmlAttribute(name = "class")
        private final String className;

        @XmlAttribute(name = "method")
        private final String methodName;

        @XmlAttribute(name = "file")
        private final String fileName;

        @XmlAttribute(name = "line")
        private final int lineNumber;

        private StackTraceElementWrapper(StackTraceElement stackTraceElement) {
            className = stackTraceElement.getClassName();
            methodName = stackTraceElement.getMethodName();
            fileName = stackTraceElement.getFileName();
            lineNumber = stackTraceElement.getLineNumber();
        }

        private StackTraceElement getStackTraceElement() {
            return new StackTraceElement(className, methodName, fileName, lineNumber);
        }
    }
}
