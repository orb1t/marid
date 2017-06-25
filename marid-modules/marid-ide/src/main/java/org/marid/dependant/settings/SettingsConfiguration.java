package org.marid.dependant.settings;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
@ComponentScan(basePackageClasses = {SettingsConfiguration.class})
public class SettingsConfiguration {

}
