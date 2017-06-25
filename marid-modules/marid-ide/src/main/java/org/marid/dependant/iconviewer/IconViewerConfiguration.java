package org.marid.dependant.iconviewer;

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@Import({IconViewer.class, IconViewerTable.class})
public class IconViewerConfiguration {


}
