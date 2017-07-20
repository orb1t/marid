package org.marid.ide.types;

import org.marid.ide.model.BeanData;
import org.marid.ide.project.ProjectProfile;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

/**
 * @author Dmitry Ovchinnikov
 */
public class BeanTypeResolverContext {

    final IdeValueConverterManager converters;
    final Map<String, BeanData> beanMap;
    final Map<String, Type> resolved = new HashMap<>();
    final LinkedHashSet<String> processing = new LinkedHashSet<>();

    public BeanTypeResolverContext(Iterable<BeanData> beans, ClassLoader classLoader) {
        converters = new IdeValueConverterManager(classLoader);
        beanMap = stream(beans.spliterator(), false).collect(toMap(BeanData::getName, identity()));
    }

    public BeanTypeResolverContext(ProjectProfile profile) {
        this(profile.getBeansFile().beans, profile.getClassLoader());
    }
}
