package org.marid.dependant.beaneditor.dao;

import org.marid.dependant.beaneditor.BeanEditorContext;
import org.marid.ide.model.BeanMethodArgData;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author Dmitry Ovchinnikov
 */
@Repository
public class ConvertersDao {

    private final ProjectProfile profile;
    private final BeanEditorContext context;

    @Autowired
    public ConvertersDao(ProjectProfile profile, BeanEditorContext context) {
        this.profile = profile;
        this.context = context;
    }

    public Set<String> getConverters(BeanMethodArgData arg) {
        final Type type = context.formalType(arg);
        return profile.getBeanCache().getConverters().getMatchedConverters(type).keySet();
    }
}
