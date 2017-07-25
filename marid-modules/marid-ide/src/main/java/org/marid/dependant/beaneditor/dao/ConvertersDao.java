package org.marid.dependant.beaneditor.dao;

import org.marid.annotation.MetaLiteral;
import org.marid.dependant.beaneditor.BeanEditorContext;
import org.marid.ide.model.BeanMethodArgData;
import org.marid.ide.project.ProjectProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Type;
import java.util.Map;

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

    public Map<String, MetaLiteral> getConverters(BeanMethodArgData arg) {
        final Type type = context.formalType(arg);
        return profile.getBeanCache().getConverters().getMatchedConverters(type);
    }
}
