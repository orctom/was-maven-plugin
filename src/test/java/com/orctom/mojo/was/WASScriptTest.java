package com.orctom.mojo.was;

import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.service.impl.WebSphereServiceScriptImpl;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by CH on 3/11/14.
 */
public class WASScriptTest {

    @Test
    public void testListApplications() throws Exception {
        WebSphereServiceScriptImpl service;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("10.164.39.41").setPort("8881").setConnectorType("SOAP").setServer("server1").setNode("ciNode02")
                    .setUser("wsadmin").setPassword("passw0rd").setProfileName("AppSrv01").setVerbose(true);

            service = new WebSphereServiceScriptImpl(model, "D:\\workspace-idea\\was-maven-plugin\\target");
            Collection<String> applications = service.listApplications();
            for (String app : applications) {
                System.out.println("app: " + app);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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


}
