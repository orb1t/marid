package org.marid.runtime.context;

import org.marid.runtime.beans.BeanMember;

/**
 * @author Dmitry Ovchinnikov
 */
class MaridContextTestUtils {

    static BeanMember m(String type, String name, String value) {
        return new BeanMember(type, name, value);
    }

    static BeanMember[] ms(BeanMember... members) {
        return members;
    }
}
