package org.marid.ide.types;

import com.google.common.reflect.TypeResolver;
import com.google.common.reflect.TypeToken;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static java.util.logging.Level.WARNING;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
public interface TypeUtilities {

    static Type classType(ClassLoader classLoader, String name) {
        try {
            final Class<?> c = Class.forName(name, false, classLoader);
            final Type t = TypeToken.of(Class.class).getSupertype(Class.class).getType();
            final ParameterizedType pt = (ParameterizedType) t;
            return new TypeResolver().where(pt.getActualTypeArguments()[0], c).resolveType(t);
        } catch (ClassNotFoundException x) {
            log(WARNING, "Unable to load class {0}", x, name);
            return null;
        }
    }
}
