package org.marid.ide.structure.editor;

import org.marid.IdeDependants;
import org.marid.dependant.beaneditor.BeanEditorConfiguration;
import org.marid.dependant.beaneditor.BeanEditorParam;
import org.marid.ide.model.TextFile;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Path;
import java.nio.file.PathMatcher;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class BeanFileEditor extends AbstractFileEditor<ProjectProfile> {

    private final ProjectManager projectManager;
    private final IdeDependants dependants;
    private final SpecialAction editAction;

    @Autowired
    public BeanFileEditor(@Qualifier("java") PathMatcher javaPathMatcher,
                          ProjectManager projectManager,
                          IdeDependants dependants,
                          SpecialAction editAction) {
        super(javaPathMatcher);
        this.projectManager = projectManager;
        this.dependants = dependants;
        this.editAction = editAction;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Bean File Editor";
    }

    @Nonnull
    @Override
    public String getIcon() {
        return icon("M_APPS");
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "bean";
    }

    @Override
    protected ProjectProfile editorContext(@Nonnull Path path) {
        return projectManager.getProfile(path).orElse(null);
    }

    @Override
    protected void edit(@Nonnull Path file, @Nonnull ProjectProfile context) {
        dependants.start(BeanEditorConfiguration.class, new BeanEditorParam(context, new TextFile(file)), c -> {});
    }

    @Nullable
    @Override
    public SpecialAction getSpecialAction() {
        return editAction;
    }
}
