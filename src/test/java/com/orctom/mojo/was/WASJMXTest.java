package com.orctom.mojo.was;

import com.orctom.mojo.was.model.Server;
import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.service.impl.WebSphereServiceJMXImpl;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

/**
 * Created by CH on 3/4/14.
 */
public class WASJMXTest {

    @Test
    public void testListServers() {
        WebSphereServiceJMXImpl service = null;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("chvwwwdevcmb01.uschecomrnd.net").setPort("8879").setConnectorType("SOAP");

            service = new WebSphereServiceJMXImpl(model);
            service.connect();
            List<Server> servers = service.listServers();
            for (Server server : servers) {
                System.out.println(server);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != service) {
                service.disconnect();
            }
        }
    }

    @Test
    public void testListServers2() {
        WebSphereServiceJMXImpl service = null;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("10.164.39.41").setPort("8880").setConnectorType("SOAP");

            service = new WebSphereServiceJMXImpl(model);
            service.connect();
            List<Server> servers = service.listServers();
            for (Server server : servers) {
                System.out.println(server);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != service) {
                service.disconnect();
            }
        }
    }

    @Test
    public void testListApplications() {
        WebSphereServiceJMXImpl service = null;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("10.164.39.41").setPort("8881").setConnectorType("SOAP").setServer("server1")
                    .setUser("wsadmin").setPassword("passw0rd")
                    .setKeyStore("C:\\Program Files (x86)\\IBM\\WebSphere\\AppServer\\profiles\\AppSrv01\\etc\\key.p12")
                    .setKeyStorePassword("WebAS")
                    .setTrustStore("C:\\Program Files (x86)\\IBM\\WebSphere\\AppServer\\profiles\\AppSrv01\\etc\\trust.p12")
                    .setTrustStorePassword("WebAS")
            ;

            service = new WebSphereServiceJMXImpl(model);
            service.connect();
            Collection applications = service.listApplications();
            for (Object application : applications) {
                System.out.println(application);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != service) {
                service.disconnect();
            }
        }
    }

    @Test
    public void restartServer() {
        WebSphereServiceJMXImpl service = null;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("localhost").setPort("8886").setConnectorType("SOAP").setServer("server1")
                    .setUser("wsadmin").setPassword("passw0rd")
                    .setKeyStore("C:\\Program Files (x86)\\IBM\\WebSphere\\AppServer\\profiles\\secure\\config\\cells\\HaoNode07Cell\\nodes\\HaoNode07\\key.p12")
                    .setKeyStorePassword("WebAS")
                    .setTrustStore("C:\\Program Files (x86)\\IBM\\WebSphere\\AppServer\\profiles\\secure\\config\\cells\\HaoNode07Cell\\nodes\\HaoNode07\\trust.p12")
                    .setTrustStorePassword("WebAS")
            ;

            System.out.println("============");
            System.out.println(model);
            System.out.println("============");

            service = new WebSphereServiceJMXImpl(model);
            service.connect();
            System.out.println("restarting.........");
            service.restartServer();
            System.out.println("restarted..........");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != service) {
                service.disconnect();
            }
        }
    }

    @Test
    public void restartServer2() {
        WebSphereServiceJMXImpl service = null;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("10.164.39.41").setPort("8881").setConnectorType("SOAP").setServer("server1")
                    .setUser("wsadmin").setPassword("passw0rd")
                    .setKeyStore("D:\\workspace-idea\\was-maven-plugin\\ssl\\key.p12")
                    .setKeyStorePassword("WebAS")
                    .setTrustStore("D:\\workspace-idea\\was-maven-plugin\\ssl\\trust.p12")
                    .setTrustStorePassword("WebAS")
            ;

            System.out.println("============");
            System.out.println(model);
            System.out.println("============");

            service = new WebSphereServiceJMXImpl(model);
            service.connect();
            System.out.println("restarting.........");
            service.restartServer();
            System.out.println("restarted..........");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != service) {
                service.disconnect();
            }
        }
    }
}
