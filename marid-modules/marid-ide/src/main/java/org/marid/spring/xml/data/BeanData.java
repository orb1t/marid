/*
 * Copyright (c) 2016 Dmitry Ovchinnikov
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

package org.marid.spring.xml.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import org.marid.jfx.util.MaridCollections;
import org.marid.spring.xml.data.collection.DCollection;
import org.marid.spring.xml.data.collection.DElement;

import javax.xml.bind.annotation.*;
import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "bean")
@XmlSeeAlso({BeanProp.class, BeanArg.class, DCollection.class})
@XmlAccessorType(XmlAccessType.NONE)
public final class BeanData extends DElement<BeanData> {

    public final StringProperty type = new SimpleStringProperty(this, "class");
    public final StringProperty name = new SimpleStringProperty(this, "name");
    public final StringProperty initMethod = new SimpleStringProperty(this, "init-method");
    public final StringProperty destroyMethod = new SimpleStringProperty(this, "destroy-method");
    public final StringProperty factoryBean = new SimpleStringProperty(this, "factory-bean");
    public final StringProperty factoryMethod = new SimpleStringProperty(this, "factory-method");
    public final StringProperty lazyInit = new SimpleStringProperty(this, "lazy-init", "default");

    public final ObservableList<BeanArg> beanArgs = MaridCollections.list();
    public final ObservableList<BeanProp> properties = MaridCollections.list();

    public BeanData() {
        type.addListener(this::invalidate);
        name.addListener(this::invalidate);
        initMethod.addListener(this::invalidate);
        destroyMethod.addListener(this::invalidate);
        factoryMethod.addListener(this::invalidate);
        factoryBean.addListener(this::invalidate);
        lazyInit.addListener(this::invalidate);

        beanArgs.addListener(this::invalidate);
        properties.addListener(this::invalidate);
    }

    @XmlAttribute(name = "class")
    public String getType() {
        return type.isEmpty().get() ? null : type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name.isEmpty().get() ? null : name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @XmlAttribute(name = "init-method")
    public String getInitMethod() {
        return initMethod.isEmpty().get() ? null : initMethod.get();
    }

    public void setInitMethod(String initMethod) {
        this.initMethod.set(initMethod);
    }

    @XmlAttribute(name = "destroy-method")
    public String getDestroyMethod() {
        return destroyMethod.isEmpty().get() ? null : destroyMethod.get();
    }

    public void setDestroyMethod(String destroyMethod) {
        this.destroyMethod.set(destroyMethod);
    }

    @XmlAttribute(name = "factory-bean")
    public String getFactoryBean() {
        return factoryBean.isEmpty().get() ? null : factoryBean.get();
    }

    public void setFactoryBean(String factoryBean) {
        this.factoryBean.set(factoryBean);
    }

    @XmlAttribute(name = "factory-method")
    public String getFactoryMethod() {
        return factoryMethod.isEmpty().get() ? null : factoryMethod.get();
    }

    public void setFactoryMethod(String factoryMethod) {
        this.factoryMethod.set(factoryMethod);
    }

    @XmlAttribute(name = "lazy-init")
    public String getLazyInit() {
        return lazyInit.get();
    }

    public void setLazyInit(String lazyInit) {
        this.lazyInit.set(lazyInit);
    }

    @XmlElement(name = "constructor-arg")
    public BeanArg[] getBeanArgs() {
        return beanArgs.stream().filter(a -> !a.isEmpty()).toArray(BeanArg[]::new);
    }

    public void setBeanArgs(BeanArg[] beanArgs) {
        this.beanArgs.addAll(beanArgs);
    }

    @XmlElement(name = "property")
    public BeanProp[] getBeanProps() {
        return properties.stream().filter(p -> !p.isEmpty()).toArray(BeanProp[]::new);
    }

    public void setBeanProps(BeanProp[] beanProps) {
        this.properties.addAll(beanProps);
    }

    public boolean isFactoryBean() {
        return factoryBean.isNotEmpty().get() || factoryMethod.isNotEmpty().get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public boolean isEmpty() {
        return (type.isEmpty().get() && factoryMethod.isEmpty().get()) || name.isEmpty().get();
    }

    public Optional<BeanProp> property(String name) {
        return properties.stream()
                .filter(p -> p.name.isEqualTo(name).get())
                .findAny();
    }
}
