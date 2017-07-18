package org.marid.runtime.converter;

import org.marid.runtime.context.MaridRuntime;

/**
 * @author Dmitry Ovchinnikov
 */
public interface ValueConvertersFactory {

    ValueConverters converters(MaridRuntime object);
}
