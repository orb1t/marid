package org.marid.swing.fonts;

import org.marid.util.Utils;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.marid.nio.ClasspathUtils.loadProperties;

/**
 * @author Dmitry Ovchinnikov
 */
public class FontAwesome {

    public static final String POM_PROPERTIES = "META-INF/maven/org.webjars/font-awesome/pom.properties";
    public static final String VERSION = loadProperties(POM_PROPERTIES).getProperty("version");
    public static Font FONT;

    static {
        final String fontResource = "/META_INF/maven/org.webjars/font-awesome/" + VERSION + "/fonts/fontawesome-webfont.ttf";
        try (final InputStream is = Utils.currentClassLoader().getResourceAsStream(fontResource)) {
            FONT = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(FONT);
        } catch (IOException | FontFormatException x) {
            throw new IllegalStateException(x);
        }
    }

    public static final Map<String, Character> CHAR_MAP = new TreeMap<>();

    static {
        final String cssResource = "/META_INF/maven/org.webjars/font-awesome/" + VERSION + "/css/font-awesome.css";
        final Pattern pattern = Pattern.compile("[.]fa-(\\w+)[:]before\\s+[{]\\s+content[:]\\s+\"(\\w+)\";[}]");
        try (final Scanner scanner = new Scanner(Utils.currentClassLoader().getResourceAsStream(cssResource))) {
            while (true) {
                final String text = scanner.findWithinHorizon(pattern, 0);
                if (text != null) {
                    final Matcher matcher = pattern.matcher(text);
                    CHAR_MAP.put(matcher.group(1), (char) Integer.parseInt(matcher.group(2).substring(1)));
                } else {
                    break;
                }
            }
        }
    }
}
