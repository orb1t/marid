package org.marid.ide;

import org.marid.ide.swing.context.GuiContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@ComponentScan(basePackageClasses = {GuiContext.class})
public class IdeConfiguration {
}
