package org.marid.runtime.converter;

/**
 * @author Dmitry Ovchinnikov
 */
@FunctionalInterface
public interface ValueConverter {

    Object convert(String value, Class<?> type);
}
