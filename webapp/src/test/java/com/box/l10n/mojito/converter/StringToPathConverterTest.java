package com.box.l10n.mojito.converter;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jaurambault
 */
public class StringToPathConverterTest {

    @Test
    public void testConvert() {
        String source = "some/path";
        StringToPathConverter instance = new StringToPathConverter();
        Path result = instance.convert(source);
        assertEquals(source, result.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testConvertNull() {
        String source = null;
        StringToPathConverter instance = new StringToPathConverter();
        instance.convert(source);
    }

    @Test
    public void testdir() {
        Path relativePath = Paths.get("");
        System.out.println(relativePath.toAbsolutePath().toString());
        Path resolvedPath = relativePath.resolve("test");
        System.out.println(resolvedPath.toAbsolutePath().toString());
    }
}
