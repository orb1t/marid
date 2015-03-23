package org.marid.ide;

import org.marid.ide.context.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
@Import({BaseContext.class, ProfileContext.class, SystemTrayContext.class, MenuContext.class, GuiContext.class})
public class IdeConfiguration {

    @Bean
    public DefaultConfigurationProvider defaultConfigurationProvider() {
        return new DefaultConfigurationProvider();
    }
}
