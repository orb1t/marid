package org.marid.ide.structure.editor;

import org.marid.ide.common.Directories;
import org.marid.ide.project.ProjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class RemoveProfileEditor extends AbstractFileEditor<Path> {

    private final ProjectManager projectManager;

    @Autowired
    public RemoveProfileEditor(Directories directories, ProjectManager projectManager) {
        super(p -> Files.isDirectory(p) && p.getParent().equals(directories.getProfiles()));
        this.projectManager = projectManager;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Remove profile";
    }

    @Nonnull
    @Override
    public String getIcon() {
        return icon("D_FOLDER_REMOVE");
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "profile";
    }

    @Nullable
    @Override
    protected Path editorContext(@Nonnull Path path) {
        return path;
    }

    @Override
    protected void edit(@Nonnull Path path, @Nonnull Path context) {
        projectManager.getProfiles().stream()
                .filter(p -> p.getName().equals(context.getFileName().toString()))
                .findFirst()
                .ifPresent(projectManager::remove);
    }
}
