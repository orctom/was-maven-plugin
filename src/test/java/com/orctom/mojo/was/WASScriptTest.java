package com.orctom.mojo.was;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.orctom.mojo.was.model.WebSphereModel;
import org.junit.Test;

import java.io.StringWriter;

/**
 * Created by CH on 3/11/14.
 */
public class WASScriptTest {

    @Test
    public void testListApplications() throws Exception {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile("installApplication.xml");

        WebSphereModel model = new WebSphereModel();
        model.setHost("10.164.39.41").setPort("8881").setConnectorType("SOAP").setServer("server1")
                .setUser("wsadmin").setPassword("passw0rd");

        StringWriter writer = new StringWriter();
        mustache.execute(writer, model).flush();
        System.out.println(writer.toString());
    }
}
