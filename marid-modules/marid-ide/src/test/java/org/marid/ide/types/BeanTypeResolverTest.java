package org.marid.ide.types;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.ide.model.BeanData;
import org.marid.ide.model.BeansFile;
import org.marid.test.NormalTests;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import java.lang.reflect.Type;
import java.util.function.Function;

import static java.util.logging.Level.INFO;
import static org.marid.logging.Log.log;

/**
 * @author Dmitry Ovchinnikov
 */
@Category({NormalTests.class})
@ContextConfiguration(classes = {BeanTypeResolverTestContext.class})
public class BeanTypeResolverTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    private Function<String, Type> typeResolver;

    @Autowired
    private BeansFile file;

    @Test
    public void allBeans() {
        for (final BeanData bean : file.beans) {
            final Type type = typeResolver.apply(bean.getName());
            log(INFO, "{0}: {1}", bean.getName(), type);
        }
    }

    @Test
    public void testBean1() {
    }
}
