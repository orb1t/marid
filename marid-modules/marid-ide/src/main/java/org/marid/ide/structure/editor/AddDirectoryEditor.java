package org.marid.ide.structure.editor;

import javafx.scene.control.TextInputDialog;
import org.marid.jfx.action.SpecialAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.util.logging.Level.WARNING;
import static org.marid.ide.IdeNotifications.n;
import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class AddDirectoryEditor extends AbstractFileEditor<Path> {

    private final SpecialAction addAction;

    @Autowired
    public AddDirectoryEditor(SpecialAction addAction) {
        super(Files::isDirectory);
        this.addAction = addAction;
    }

    @Override
    protected void edit(@Nonnull Path file, @Nonnull Path context) {
        final TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(s("Add a directory"));
        dialog.setContentText(s("Directory name") + ":");
        final Optional<String> optionalDirectoryName = dialog.showAndWait();
        if (optionalDirectoryName.isPresent()) {
            final String dirName = optionalDirectoryName.get();
            final Path path = file.resolve(dirName);
            try {
                Files.createDirectory(path);
            } catch (Exception x) {
                n(WARNING, "Unable to create a directory {0}", x, path);
            }
        }
    }

    @Nonnull
    @Override
    public String getName() {
        return "Add a directory";
    }

    @Nonnull
    @Override
    public String getIcon() {
        return icon("M_ADD_CIRCLE");
    }

    @Nonnull
    @Override
    public String getGroup() {
        return "file";
    }

    @Override
    protected Path editorContext(@Nonnull Path path) {
        return path;
    }

    @Nullable
    @Override
    public SpecialAction getSpecialAction() {
        return addAction;
    }
}
