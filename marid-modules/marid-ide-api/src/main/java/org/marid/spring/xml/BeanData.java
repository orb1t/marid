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

import org.marid.jfx.beans.FxList;
import org.marid.jfx.beans.FxObservable;
import org.marid.jfx.beans.FxString;

import javax.xml.bind.annotation.*;
import java.lang.reflect.Executable;
import java.util.stream.Stream;

/**
 * @author Dmitry Ovchinnikov
 */
@XmlRootElement(name = "bean")
@XmlSeeAlso({BeanProp.class, DCollection.class, Meta.class})
@XmlAccessorType(XmlAccessType.NONE)
public final class BeanData extends DElement<BeanData> {

    public final FxString type = new FxString(null, "class");
    public final FxString name = new FxString(null, "name");
    public final FxString initMethod = new FxString(null, "init-method");
    public final FxString destroyMethod = new FxString(null, "destroy-method");
    public final FxString factoryBean = new FxString(null, "factory-bean");
    public final FxString factoryMethod = new FxString(null, "factory-method");
    public final FxString lazyInit = new FxString(null, "lazy-init", "default");
    public final FxList<BeanArg> beanArgs = new FxList<>(BeanArg::observables);
    public final FxList<BeanProp> properties = new FxList<>(BeanProp::observables);
    public final transient FxList<Executable> constructors = new FxList<>();

    @XmlAttribute(name = "class")
    public String getType() {
        return type.get() == null || type.get().isEmpty() ? null : type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name.get() == null || name.get().isEmpty() ? null : name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @XmlAttribute(name = "init-method")
    public String getInitMethod() {
        return initMethod.get() == null || initMethod.get().isEmpty() ? null : initMethod.get();
    }

    public void setInitMethod(String initMethod) {
        this.initMethod.set(initMethod);
    }

    @XmlAttribute(name = "destroy-method")
    public String getDestroyMethod() {
        return destroyMethod.get() == null || destroyMethod.get().isEmpty() ? null : destroyMethod.get();
    }

    public void setDestroyMethod(String destroyMethod) {
        this.destroyMethod.set(destroyMethod);
    }

    @XmlAttribute(name = "factory-bean")
    public String getFactoryBean() {
        return factoryBean.get() == null || factoryBean.get().isEmpty() ? null : factoryBean.get();
    }

    public void setFactoryBean(String factoryBean) {
        this.factoryBean.set(factoryBean);
    }

    @XmlAttribute(name = "factory-method")
    public String getFactoryMethod() {
        return factoryMethod.get() == null || factoryMethod.get().isEmpty() ? null : factoryMethod.get();
    }

    public void setFactoryMethod(String factoryMethod) {
        this.factoryMethod.set(factoryMethod);
    }

    @XmlAttribute(name = "lazy-init")
    public String getLazyInit() {
        return "default".equals(lazyInit.get()) ? null : lazyInit.get();
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

    public boolean isFactoryBean() {
        if (factoryBean.get() != null && !factoryBean.get().isEmpty()) {
            return factoryMethod.get() != null && !factoryMethod.get().isEmpty();
        } else if (factoryMethod.get() != null && !factoryMethod.get().isEmpty()) {
            return type.get() != null && !type.get().isEmpty();
        } else {
            return false;
        }
    }

    public boolean isEmpty() {
        if (name.get() == null || name.get().isEmpty()) {
            return true;
        }
        if ((type.get() == null || type.get().isEmpty()) && (factoryMethod.get() == null || factoryMethod.get().isEmpty())) {
            return true;
        }
        return false;
    }

    @Override
    public FxObservable[] observables() {
        return new FxObservable[]{
                type,
                name,
                initMethod,
                destroyMethod,
                factoryBean,
                factoryMethod,
                lazyInit,
                beanArgs,
                properties,
                constructors
        };
    }

    @Override
    public Stream<FxObservable> observableStream() {
        return Stream.of(observables());
    }

    @Override
    public Stream<? extends AbstractData<?>> stream() {
        return Stream.concat(beanArgs.stream(), properties.stream());
    }

    @Override
    public String toString() {
        return name.get();
    }
}
