package org.marid.ide;

import org.marid.ide.context.BaseContext;
import org.marid.ide.context.GuiContext;
import org.marid.ide.context.ProfileContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@Import({BaseContext.class, ProfileContext.class, GuiContext.class})
public class IdeConfiguration {

}
