package com.orctom.mojo.was.utils;

import junit.framework.Assert;
import org.junit.Test;

import java.util.Properties;

/**
 * Created by CH on 3/20/14.
 */
public class PropertiesUtilsTest {

    @Test
    public void testResolve() {
        Properties properties = new Properties();
        properties.setProperty("aa", "aaaa");
        properties.setProperty("bb", "bbbb");
        String resolved = PropertiesUtils.resolve("hello ${aa} ${xx} ${bb}", properties, "${", "}");
        Assert.assertEquals("hello aaaa ${xx} bbbb", resolved);
    }
}
