package org.marid.dependant.beaneditor.model;

import org.marid.runtime.beans.Bean;

public class WildBean {

    public final Bean bean;

    public WildBean(Bean bean) {
        this.bean = bean;
    }

    public WildBean(LibraryBean bean) {
        this(bean.bean);
    }

    @Override
    public String toString() {
        return bean.producer.signature;
    }
}
