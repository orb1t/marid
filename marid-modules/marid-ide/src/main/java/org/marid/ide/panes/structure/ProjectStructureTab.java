package org.marid.ide.panes.structure;

import org.marid.ide.tabs.IdeTab;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import static org.marid.jfx.LocalizedStrings.ls;
import static org.marid.jfx.icons.FontIcons.glyphIcon;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Lazy(false)
public class ProjectStructureTab extends IdeTab {

    @Autowired
    public ProjectStructureTab(ProjectStructurePane projectStructurePane) {
        super(projectStructurePane, ls("Profiles"), () -> glyphIcon("O_BOOK", 16));
        setClosable(false);
    }
}
