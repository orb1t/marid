package org.marid.spring.dependant;

import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import java.io.IOException;

/**
 * @author Dmitry Ovchinnikov
 */
public class IdeClassFilter extends TypeExcludeFilter {

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
        final String className = metadataReader.getClassMetadata().getClassName();
        return !className.startsWith("org.marid.ide");
    }
}
