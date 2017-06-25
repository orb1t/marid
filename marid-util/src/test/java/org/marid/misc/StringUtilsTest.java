package org.marid.misc;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.marid.test.NormalTests;

import static org.junit.Assert.assertEquals;
import static org.marid.misc.StringUtils.camelToText;
import static org.marid.misc.StringUtils.delimited;

/**
 * @author Dmitry Ovchinnikov
 */
@Category(NormalTests.class)
public class StringUtilsTest {

    @Test
    public void testDelimited() {
        assertEquals("a,b,c", delimited(',', "a", "b", "c"));
        assertEquals("a  b  c", delimited("  ", "a", "b", "c"));
        assertEquals("a", delimited(',', "a"));
        assertEquals("", delimited(','));
        assertEquals("1,2.3,4.5,q", delimited(',', 1, 2.3, 4.5f, 'q'));
        assertEquals("1++2.3++4.5++q", delimited("++", 1, 2.3, 4.5f, 'q'));
    }

    @Test
    public void testCamelToText() {
        assertEquals("Camel Test", camelToText("camelTest"));
        assertEquals("Camel Test", camelToText(" camelTest"));
        assertEquals("Camel COOL Test", camelToText(" camelCOOLTest"));
        assertEquals("Верблюжий Тест", camelToText("верблюжийТест"));
        assertEquals("Верблюжий Т", camelToText("верблюжийТ"));
        assertEquals("Camel TEST", camelToText("camelTEST"));
    }
}
