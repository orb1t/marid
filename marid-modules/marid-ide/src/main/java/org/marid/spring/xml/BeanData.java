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
import org.marid.jfx.beans.FxList;
import org.marid.jfx.beans.FxString;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import java.lang.reflect.Executable;

import static java.util.Optional.ofNullable;
import static org.marid.misc.Iterables.nodes;

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
    public final FxList<BeanArg> beanArgs = new FxList<>(a -> new Observable[] {a});
    public final FxList<BeanProp> properties = new FxList<>(p -> new Observable[] {p});
    public final FxList<Meta> meta = new FxList<>(p -> new Observable[] {p});
    public final transient FxList<Executable> constructors = new FxList<>();

    public BeanData() {
        type.addListener(this::fireInvalidate);
        name.addListener(this::fireInvalidate);
        initMethod.addListener(this::fireInvalidate);
        destroyMethod.addListener(this::fireInvalidate);
        factoryBean.addListener(this::fireInvalidate);
        factoryMethod.addListener(this::fireInvalidate);
        lazyInit.addListener(this::fireInvalidate);
        beanArgs.addListener(this::fireInvalidate);
        properties.addListener(this::fireInvalidate);
    }

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
    public String toString() {
        return name.get();
    }

    @Override
    public void loadFrom(Document document, Element element) {
        ofNullable(element.getAttribute("class")).ifPresent(type::set);
        ofNullable(element.getAttribute("name")).ifPresent(name::set);
        ofNullable(element.getAttribute("init-method")).ifPresent(initMethod::set);
        ofNullable(element.getAttribute("destroy-method")).ifPresent(destroyMethod::set);
        ofNullable(element.getAttribute("factory-bean")).ifPresent(factoryBean::set);
        ofNullable(element.getAttribute("factory-method")).ifPresent(factoryMethod::set);
        ofNullable(element.getAttribute("lazy-init")).ifPresent(lazyInit::set);
        nodes(element, Element.class).filter(e -> "constructor-arg".equals(e.getTagName())).forEach(e -> {
            final BeanArg arg = new BeanArg();
            arg.loadFrom(document, e);
            beanArgs.add(arg);
        });
        nodes(element, Element.class).filter(e -> "property".equals(e.getTagName())).forEach(e -> {
            final BeanProp prop = new BeanProp();
            prop.loadFrom(document, e);
            properties.add(prop);
        });
        nodes(element, Element.class).filter(e -> "meta".equals(e.getTagName())).forEach(e -> {
            final Meta meta = new Meta();
            meta.loadFrom(document, e);
            this.meta.add(meta);
        });
    }

    @Override
    public void writeTo(Document document, Element element) {
        ofNullable(type.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("class", e));
        ofNullable(name.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("name", e));
        ofNullable(initMethod.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("init-method", e));
        ofNullable(destroyMethod.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("destroy-method", e));
        ofNullable(factoryBean.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("factory-bean", e));
        ofNullable(factoryMethod.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("factory-method", e));
        ofNullable(lazyInit.get()).filter(s -> !s.isEmpty()).ifPresent(e -> element.setAttribute("lazy-init", e));
        beanArgs.stream().filter(a -> !a.isEmpty()).forEach(a -> {
            final Element e = document.createElement("constructor-arg");
            a.writeTo(document, e);
            element.appendChild(e);
        });
        properties.stream().filter(p -> !p.isEmpty()).forEach(p -> {
            final Element e = document.createElement("property");
            p.writeTo(document, e);
            element.appendChild(e);
        });
        meta.forEach(m -> {
            final Element e = document.createElement("meta");
            m.writeTo(document, e);
            element.appendChild(e);
        });
    }
}
