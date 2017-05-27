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

import org.apache.commons.lang3.ArrayUtils;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.beans.OOList;
import org.marid.jfx.beans.OString;
import org.marid.misc.Calls;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.ResolvableType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparingInt;
import static java.util.Optional.ofNullable;
import static java.util.stream.Stream.of;
import static org.marid.misc.Iterables.nodes;
import static org.springframework.core.ResolvableType.forMethodParameter;
import static org.springframework.core.ResolvableType.forMethodReturnType;

/**
 * @author Dmitry Ovchinnikov
 */
public final class BeanData extends DElement {

    private static final ParameterNameDiscoverer PND = new DefaultParameterNameDiscoverer();

    public final OString type = new OString("class");
    public final OString name = new OString("name");
    public final OString initMethod = new OString("init-method");
    public final OString destroyMethod = new OString("destroy-method");
    public final OString factoryBean = new OString("factory-bean");
    public final OString factoryMethod = new OString("factory-method");
    public final OString lazyInit = new OString("lazy-init", "default");
    public final OOList<BeanArg> beanArgs = new OOList<>();
    public final OOList<BeanProp> properties = new OOList<>();
    public final OOList<Meta> meta = new OOList<>();

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

    public String getType() {
        return type.get() == null || type.get().isEmpty() ? null : type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public String getName() {
        return name.get() == null || name.get().isEmpty() ? null : name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getInitMethod() {
        return initMethod.get() == null || initMethod.get().isEmpty() ? null : initMethod.get();
    }

    public void setInitMethod(String initMethod) {
        this.initMethod.set(initMethod);
    }

    public String getDestroyMethod() {
        return destroyMethod.get() == null || destroyMethod.get().isEmpty() ? null : destroyMethod.get();
    }

    public void setDestroyMethod(String destroyMethod) {
        this.destroyMethod.set(destroyMethod);
    }

    public String getFactoryBean() {
        return factoryBean.get() == null || factoryBean.get().isEmpty() ? null : factoryBean.get();
    }

    public void setFactoryBean(String factoryBean) {
        this.factoryBean.set(factoryBean);
    }

    public String getFactoryMethod() {
        return factoryMethod.get() == null || factoryMethod.get().isEmpty() ? null : factoryMethod.get();
    }

    public void setFactoryMethod(String factoryMethod) {
        this.factoryMethod.set(factoryMethod);
    }

    public String getLazyInit() {
        return "default".equals(lazyInit.get()) ? null : lazyInit.get();
    }

    public void setLazyInit(String lazyInit) {
        this.lazyInit.set(lazyInit == null ? "default" : lazyInit);
    }

    public BeanArg[] getBeanArgs() {
        return beanArgs.stream().filter(a -> !a.isEmpty()).toArray(BeanArg[]::new);
    }

    public void setBeanArgs(BeanArg[] beanArgs) {
        this.beanArgs.setAll(beanArgs);
    }

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
    public void loadFrom(Document document, Element element) {
        Optional.of(element.getAttribute("class")).filter(s -> !s.isEmpty()).ifPresent(type::set);
        Optional.of(element.getAttribute("name")).filter(s -> !s.isEmpty()).ifPresent(name::set);
        Optional.of(element.getAttribute("init-method")).filter(s -> !s.isEmpty()).ifPresent(initMethod::set);
        Optional.of(element.getAttribute("destroy-method")).filter(s -> !s.isEmpty()).ifPresent(destroyMethod::set);
        Optional.of(element.getAttribute("factory-bean")).filter(s -> !s.isEmpty()).ifPresent(factoryBean::set);
        Optional.of(element.getAttribute("factory-method")).filter(s -> !s.isEmpty()).ifPresent(factoryMethod::set);
        Optional.of(element.getAttribute("lazy-init")).filter(s -> !s.isEmpty()).ifPresent(lazyInit::set);
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

    private int argsMatchCount(String[] argNames, String[] actualNames) {
        final String[] a1 = of(argNames).filter(n -> ArrayUtils.contains(actualNames, n)).toArray(String[]::new);
        final String[] a2 = of(actualNames).filter(n -> ArrayUtils.contains(a1, n)).toArray(String[]::new);
        final int n = Math.min(a1.length, a2.length);
        for (int i = 0; i < n; i++) {
            if (!a1[i].equals(a2[i])) {
                return i;
            }
        }
        return n;
    }

    @Override
    protected void refresh(ProjectProfile profile, Set<Object> passed) {
        if (!passed.add(this)) {
            return;
        }
        final String[] argNames = beanArgs.stream().map(BeanArg::getName).toArray(String[]::new);
        final String[] actualNames;
        final Executable executable;
        final ResolvableType constructorType;
        final int argMatches;
        if (!isFactoryBean()) {
            final Class<?> raw = profile.getClass(type.get()).orElse(null);
            if (raw == null) {
                return;
            }
            final Comparator<Constructor<?>> cc = comparingInt(c -> argsMatchCount(argNames, PND.getParameterNames(c)));
            final Constructor<?>[] constructors = of(raw.getConstructors()).sorted(cc).toArray(Constructor<?>[]::new);
            if (constructors.length == 0) {
                return;
            }
            final Constructor<?> constructor = constructors[constructors.length - 1];
            executable = constructor;
            constructorType = ResolvableType.forClass(raw);
            argMatches = argsMatchCount(argNames, actualNames = PND.getParameterNames(constructor));
        } else if (getFactoryBean() != null && !getFactoryBean().isEmpty()) {
            final BeanData refBean = profile.getBeanFiles().stream()
                    .flatMap(f -> f.beans.stream())
                    .filter(b -> getFactoryBean().equals(b.getName()))
                    .findFirst()
                    .orElse(null);
            if (refBean == null) {
                return;
            }
            refBean.refresh(profile, passed);
            if (refBean.resolvableType.get() == ResolvableType.NONE) {
                return;
            }
            final Class<?> refClass = refBean.resolvableType.get().getRawClass();
            if (refClass == null) {
                return;
            }
            final Method[] methods = of(refClass.getMethods())
                    .filter(m -> !Modifier.isStatic(m.getModifiers()))
                    .filter(m -> m.getName().equals(getFactoryMethod()))
                    .sorted(comparingInt(m -> argsMatchCount(argNames, PND.getParameterNames(m))))
                    .toArray(Method[]::new);
            if (methods.length == 0) {
                return;
            }
            final Method method = methods[methods.length - 1];
            executable = method;
            constructorType = forMethodReturnType(methods[0], refClass);
            argMatches = argsMatchCount(argNames, actualNames = PND.getParameterNames(method));
        } else {
            final Class<?> raw = profile.getClass(type.get()).orElse(null);
            if (raw == null) {
                return;
            }
            final Method[] methods = of(raw.getMethods())
                    .filter(m -> Modifier.isStatic(m.getModifiers()))
                    .filter(m -> m.getName().equals(getFactoryMethod()))
                    .sorted(comparingInt(m -> argsMatchCount(argNames, PND.getParameterNames(m))))
                    .toArray(Method[]::new);
            if (methods.length == 0) {
                return;
            }
            final Method method = methods[methods.length - 1];
            executable = method;
            constructorType = forMethodReturnType(methods[0], raw);
            argMatches = argsMatchCount(argNames, actualNames = PND.getParameterNames(method));
        }

        final Parameter[] parameters = executable.getParameters();
        beanArgs.removeIf(a -> of(parameters).noneMatch(p -> p.getName().equals(a.getName())));
        beanArgs.remove(argMatches, beanArgs.size());
        for (int i = 0; i < parameters.length; i++) {
            if (i < beanArgs.size()) {
                if (!actualNames[i].equals(beanArgs.get(i).getName())) {
                    final BeanArg arg = new BeanArg();
                    arg.setName(actualNames[i]);
                    beanArgs.add(i, arg);
                    arg.refresh(profile, passed);
                } else {
                    beanArgs.get(i).refresh(profile, passed);
                }
            } else {
                final BeanArg arg = new BeanArg();
                arg.setName(actualNames[i]);
                beanArgs.add(arg);
                arg.refresh(profile, passed);
            }
        }

        final BeanInfo beanInfo = Calls.call(() -> Introspector.getBeanInfo(constructorType.getRawClass()));
        final PropertyDescriptor[] props = beanInfo.getPropertyDescriptors();
        for (final PropertyDescriptor property : props) {
            if (property.getWriteMethod() != null) {
                final BeanProp prop = properties.stream()
                        .filter(p -> property.getName().equals(p.getName()))
                        .findFirst()
                        .orElseGet(() -> {
                            final BeanProp beanProp = new BeanProp();
                            beanProp.setName(property.getName());
                            properties.add(beanProp);
                            return beanProp;
                        });
                final MethodParameter mp = new MethodParameter(property.getWriteMethod(), 0);
                prop.resolvableType.set(forMethodParameter(mp, constructorType));

                prop.refresh(profile, passed);
            }
        }

        resolvableType.set(constructorType);
    }

    @Override
    public String toString() {
        return name.get();
    }
}
