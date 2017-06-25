package org.marid.ide.settings;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * @author Dmitry Ovchinnikov
 */
@Component
public class JavaSettings extends AbstractSettings {

    public final Path java = Paths.get(
            System.getProperty("java.home"),
            "bin",
            "java" + (System.getProperty("os.name").contains("indows") ? ".exe" : "")
    );

    public String getJavaExecutable() {
        return preferences.get("javaExecutable", java.toString());
    }

    public void setJavaExecutable(String value) {
        if (StringUtils.isBlank(value)) {
            preferences.remove("javaExecutable");
        } else {
            preferences.put("javaExecutable", value);
        }
    }

    public String[] getJavaArguments() {
        return Optional.ofNullable(preferences.get("javaArguments", null)).map(s -> s.split("\\000")).orElse(new String[0]);
    }

    public void setJavaArguments(String[] arguments) {
        preferences.put("javaArguments", String.join("\000", arguments));
    }

    @Override
    public String getName() {
        return "Java";
    }
}
