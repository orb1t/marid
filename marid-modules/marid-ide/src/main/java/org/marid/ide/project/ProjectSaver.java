package org.marid.ide.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class ProjectSaver {

    private final ProjectPrerequisites projectPrerequisites;

    @Autowired
    public ProjectSaver(ProjectPrerequisites projectPrerequisites) {
        this.projectPrerequisites = projectPrerequisites;
    }

    public void save(ProjectProfile profile) {
        projectPrerequisites.apply(profile);
        profile.save();
    }
}
