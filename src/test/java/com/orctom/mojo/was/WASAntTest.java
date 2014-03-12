package com.orctom.mojo.was;

import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.service.impl.WebSphereServiceAntImpl;
import org.junit.Test;

import java.util.Collection;

/**
 * Created by CH on 3/11/14.
 */
public class WASAntTest {

    @Test
    public void testListApplications() throws Exception {
        WebSphereServiceAntImpl service;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("10.164.39.41").setPort("8881").setConnectorType("SOAP").setServer("server1").setNode("ciNode02")
                    .setUser("wsadmin").setPassword("passw0rd").setProfileName("AppSrv01").setVerbose(true);

            service = new WebSphereServiceAntImpl(model, "D:\\workspace-idea\\was-maven-plugin\\target");
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
        WebSphereServiceAntImpl service;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("10.164.39.41").setPort("8881").setConnectorType("SOAP").setServer("server1").setNode("ciNode02")
                    .setUser("wsadmin").setPassword("passw0rd").setProfileName("AppSrv01").setVerbose(true);

            service = new WebSphereServiceAntImpl(model, "D:\\workspace-idea\\was-maven-plugin\\target");
            service.startServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
