package org.marid.dependant.beaneditor.model;

import org.marid.annotation.MetaLiteral;
import org.marid.runtime.beans.Bean;

/**
 * @author Dmitry Ovchinnikov
 */
public class LibraryBean {

    public final Bean bean;
    public final MetaLiteral literal;

    public LibraryBean(Bean bean, MetaLiteral literal) {
        this.bean = bean;
        this.literal = literal;
    }
}
