package com.orctom.mojo.was.utils;

import junit.framework.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.Map;
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

    @Test
    public void testLoad() {
        Properties defaultProps = new Properties();
        defaultProps.setProperty("aa", "aaaa");
        defaultProps.setProperty("bb", "bbbb");
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("properties-utiles-test.properties");
        Map<String, Properties> propertiesMap = PropertiesUtils.loadSectionedProperties(in, defaultProps);
        Assert.assertEquals("aaaa", propertiesMap.get("www.dev.trunk").getProperty("aa"));
        Assert.assertEquals("od", propertiesMap.get("www.dev.trunk").getProperty("meta.brand"));
        Assert.assertEquals("wwwdev-trunk-cluster", propertiesMap.get("www.dev.trunk").getProperty("cluster"));
        Assert.assertEquals("wwwdev-branch-cluster", propertiesMap.get("www.dev.branch").getProperty("cluster"));
        for (String section : propertiesMap.keySet()) {
            System.out.println("section: ================== " + section);
            Properties prop = propertiesMap.get(section);
            for (Map.Entry<Object, Object> entry : prop.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }
    }
}
