package org.marid.ide.structure.editor;

import org.marid.ide.common.Directories;
import org.marid.ide.project.ProjectManager;
import org.marid.ide.project.ProjectProfile;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.util.logging.Level.WARNING;
import static org.marid.ide.IdeNotifications.n;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class RemoveFileEditor extends AbstractFileEditor<Path> {

    private final SpecialAction removeAction;

    @Autowired
    public RemoveFileEditor(Directories directories, ProjectManager projectManager, SpecialAction removeAction) {
        super(path -> Stream.concat(
                Stream.of(directories.getProfiles()),
                projectManager.getProfiles().stream().map(ProjectProfile::getPath)
        ).noneMatch(path::equals));
        this.removeAction = removeAction;
    }

    @Nonnull
    @Override
    public String getName() {
        return "Remove file";
    }

    @Nonnull
    @Override
    public String getIcon() {
        return icon("M_REMOVE_CIRCLE");
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "file";
    }

    @Override
    protected void edit(@Nonnull Path file, @Nonnull Path path) {
        try {
            Files.delete(file);
        } catch (Exception x) {
            n(WARNING, "Unable to delete {0}", x, file);
        }
    }

    @Override
    protected Path editorContext(@Nonnull Path path) {
        return path;
    }

    @Nullable
    @Override
    public SpecialAction getSpecialAction() {
        return removeAction;
    }
}
