package org.marid.editors.url;

import javafx.stage.FileChooser.ExtensionFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import static org.marid.l10n.L10n.s;

/**
 * @author Dmitry Ovchinnikov.
 * @since 0.8
 */
@Configuration
public class SvgFilters {

    @Bean
    @Order(1)
    public ExtensionFilter svgExtensionFilter() {
        return new ExtensionFilter(s("SVG files"), "*.svg");
    }

    @Bean
    @Order(2)
    public ExtensionFilter svgzExtensionFilter() {
        return new ExtensionFilter(s("Compressed SVG files"), "*.svgz");
    }
}
