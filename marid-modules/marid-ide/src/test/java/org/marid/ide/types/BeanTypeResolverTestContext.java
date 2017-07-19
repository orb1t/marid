package org.marid.ide.types;

import org.marid.ide.model.BeanData;
import org.marid.ide.model.BeanMemberData;
import org.marid.ide.model.BeansFile;
import org.marid.runtime.context.MaridConfiguration;
import org.marid.runtime.context.MaridContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.lang.Thread.currentThread;
import static org.marid.misc.Builder.build;
import static org.marid.misc.Calls.call;
import static org.marid.runtime.beans.MaridFactoryBean.producer;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class BeanTypeResolverTestContext {

    @Bean
    public BeanTypeResolver beanTypeResolver() {
        return new BeanTypeResolver();
    }

    @Bean
    public BeanData bean1() {
        return build(new BeanData(), b -> {
            b.name.set("bean1");
            b.factory.set(ArrayList.class.getName());
            b.producer.set(producer(call(() -> ArrayList.class.getConstructor(Collection.class))));
            b.args.add(build(new BeanMemberData(), a -> {
                a.name.set("arg0");
                a.type.set("ref");
                a.value.set("bean2");
            }));
        });
    }

    @Bean
    public BeanData bean2() {
        return build(new BeanData(), b -> {
            b.name.set("bean2");
            b.factory.set(Arrays.class.getName());
            b.producer.set(producer(call(() -> Arrays.class.getMethod("asList", Object[].class))));
            b.args.add(build(new BeanMemberData(), a -> {
                a.name.set("arg0");
                a.type.set("String[]");
                a.value.set("a,b,c");
            }));
        });
    }

    @Bean
    public BeansFile beansFile(List<BeanData> beans) {
        return build(new BeansFile(), f -> f.beans.setAll(beans));
    }

    @Bean
    public MaridContext maridContext(BeansFile beansFile) {
        return new MaridContext(new MaridConfiguration(beansFile.toBeans()));
    }

    @Bean
    public Function<String, Type> typeResolver(BeanTypeResolver resolver, BeansFile file) {
        return name -> resolver.resolve(file.beans, currentThread().getContextClassLoader(), name);
    }
}
