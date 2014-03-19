package com.orctom.mojo.was.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

/**
 * Created by CH on 3/19/14.
 */
public class PropertiesUtils {

    public static Properties loadProperties(URL url) {
        try {
            return loadProperties(url.openStream());
        } catch (Exception e) {
            return null;
        }
    }

    public static Properties loadProperties(File file) {
        try {
            return loadProperties(new FileInputStream(file));
        } catch (Exception e) {
            return null;
        }

    }

    public static Properties loadProperties(InputStream is) {
        try {
            Properties properties = new Properties();

            if (null != is) {
                properties.load(is);
            }

            return properties;
        } catch (IOException e) {
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
            }
        }
    }

    public static String resolve(String expression, Properties properties) {
        return resolve(expression, properties, "{{", "}}");
    }

    public static String resolve(String expression, Properties properties, String startSign, String endSign) {
        StringBuilder template = new StringBuilder(expression);

        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            int start;
            String pattern = startSign + entry.getKey() + endSign;
            String value = entry.getValue().toString();

            while ((start = template.indexOf(pattern)) != -1) {
                template.replace(start, start + pattern.length(), value);
            }
        }

        return template.toString();
    }
}
