package com.orctom.mojo.was;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.service.impl.WebSphereServiceScriptImpl;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

/**
 * Created by CH on 3/11/14.
 */
public class WASScriptTest {

    @Test
    public void testRestartServer() throws Exception {
        WebSphereServiceScriptImpl service;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("10.164.39.41").setPort("8881").setConnectorType("SOAP").setServer("server1").setNode("ciNode02")
                    .setUser("wsadmin").setPassword("passw0rd").setProfileName("AppSrv01").setVerbose(true);

            service = new WebSphereServiceScriptImpl(model, "D:\\workspace-idea\\was-maven-plugin\\target");
            service.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDeploy() throws Exception {
        WebSphereServiceScriptImpl service;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("10.164.39.41").setPort("8881").setConnectorType("SOAP").setServer("server1").setNode("ciNode02")
                    .setUser("wsadmin").setPassword("passw0rd").setProfileName("AppSrv01").setApplicationName("test")
                    .setPackageFile("D:\\workspace-idea\\was-maven-plugin\\scripts\\DefaultApplication.ear").setVerbose(true);

            service = new WebSphereServiceScriptImpl(model, "D:\\workspace-idea\\was-maven-plugin\\target");
            service.deploy();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testResolveProps() throws IOException {
        Properties props = new Properties();
        props.setProperty("abc.abc", "aaa");
        props.setProperty("edf.edf", "bbb");
        props.setProperty("hello", "hello {{abc}} {{edf}}");
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new StringReader("hello {{abc\\.abc}} {{edf\\.edf}}"), "hello");
        StringWriter writer = new StringWriter();
        mustache.execute(writer, props).flush();
        System.out.println(writer.toString());
    }

}
