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

package org.marid.spring.xml;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.bind.annotation.*;
import java.lang.reflect.Executable;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.IntStream.range;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "bean")
@XmlSeeAlso({BeanProp.class, DCollection.class, Meta.class})
@XmlAccessorType(XmlAccessType.NONE)
public final class BeanData extends DElement<BeanData> {

    public final StringProperty type = new SimpleStringProperty(this, "class");
    public final StringProperty name = new SimpleStringProperty(this, "name");
    public final StringProperty initMethod = new SimpleStringProperty(this, "init-method");
    public final StringProperty destroyMethod = new SimpleStringProperty(this, "destroy-method");
    public final StringProperty factoryBean = new SimpleStringProperty(this, "factory-bean");
    public final StringProperty factoryMethod = new SimpleStringProperty(this, "factory-method");
    public final StringProperty lazyInit = new SimpleStringProperty(this, "lazy-init", "default");

    public final ObservableList<BeanArg> beanArgs = FXCollections.observableArrayList(BeanArg::observables);
    public final ObservableList<BeanProp> properties = FXCollections.observableArrayList(BeanProp::observables);

    public final ObservableList<String> initTriggers = FXCollections.observableArrayList();
    public final ObservableList<String> destroyTriggers = FXCollections.observableArrayList();

    public final transient ObjectProperty<Executable> constructor = new SimpleObjectProperty<>(this, "constructor");
    public final transient ObservableList<Executable> constructors = FXCollections.observableArrayList();

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
        return lazyInit.isEqualTo("default").get() ? null : lazyInit.get();
    }

    public void setLazyInit(String lazyInit) {
        this.lazyInit.set(lazyInit == null ? "default" : lazyInit);
    }

    @XmlElement(name = "constructor-arg")
    public BeanArg[] getBeanArgs() {
        return beanArgs.stream().filter(a -> !a.isEmpty()).toArray(BeanArg[]::new);
    }

    public void setBeanArgs(BeanArg[] beanArgs) {
        this.beanArgs.setAll(beanArgs);
    }

    @XmlElement(name = "property")
    public BeanProp[] getBeanProps() {
        return properties.stream().filter(p -> !p.isEmpty()).toArray(BeanProp[]::new);
    }

    public void setBeanProps(BeanProp[] beanProps) {
        this.properties.setAll(beanProps);
    }

    @XmlAnyElement(lax = true)
    public Meta[] getMeta() {
        return Stream.concat(
                range(0, initTriggers.size()).mapToObj(i -> new Meta("init" + i, initTriggers.get(i))),
                range(0, destroyTriggers.size()).mapToObj(i -> new Meta("destroy" + i, destroyTriggers.get(i)))
        ).toArray(Meta[]::new);
    }

    public void setMeta(Meta[] metas) {
        for (final Meta meta : metas) {
            if (meta.key.startsWith("init")) {
                initTriggers.add(meta.value);
            } else if (meta.key.startsWith("destroy")) {
                destroyTriggers.add(meta.value);
            }
        }
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

    public Optional<BeanArg> arg(String name) {
        return beanArgs.stream()
                .filter(a -> a.name.isEqualTo(name).get())
                .findAny();
    }

    @Override
    public Observable[] observables() {
        return new Observable[] {
                type,
                name,
                initMethod,
                destroyMethod,
                factoryBean,
                factoryMethod,
                lazyInit,
                beanArgs,
                properties,
                initTriggers,
                destroyTriggers
        };
    }

    @Override
    public String toString() {
        return name.get();
    }
}
