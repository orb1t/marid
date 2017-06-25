package org.marid.ide.common;

import com.github.javaparser.printer.PrettyPrinter;
import com.github.javaparser.printer.PrettyPrinterConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class JavaParsers {

    @Bean
    public PrettyPrinterConfiguration prettyPrinterConfiguration() {
        return new PrettyPrinterConfiguration()
                .setIndent("  ")
                .setPrintComments(true)
                .setEndOfLineCharacter("\n");
    }

    @Bean
    public PrettyPrinter prettyPrinter(PrettyPrinterConfiguration configuration) {
        return new PrettyPrinter(configuration);
    }
}
