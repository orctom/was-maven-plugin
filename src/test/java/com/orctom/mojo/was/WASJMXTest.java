package com.orctom.mojo.was;

import com.orctom.mojo.was.model.Server;
import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.service.jmx.WebSphereService;
import org.junit.Test;

import java.util.List;
import java.util.Vector;

/**
 * Created by CH on 3/4/14.
 */
public class WASJMXTest {

    @Test
    public void testListServers() {
        WebSphereService service = null;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("chvwwwdevcmb01.uschecomrnd.net").setPort("8879").setConnectorType("SOAP");

            service = new WebSphereService(model);
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
        WebSphereService service = null;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("10.164.39.41").setPort("8880").setConnectorType("SOAP");

            service = new WebSphereService(model);
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
        WebSphereService service = null;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("10.164.39.41").setPort("8880").setConnectorType("SOAP");

            service = new WebSphereService(model);
            service.connect();
            Vector applications = service.listApplications();
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
    public void refreshCluster() {
        WebSphereService service = null;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("chvwwwdevcmb01.uschecomrnd.net").setPort("8879").setConnectorType("SOAP").setCluster("wwwdev-trunk-cluster");

            service = new WebSphereService(model);
            service.connect();
            service.refreshCluster();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != service) {
                service.disconnect();
            }
        }
    }

    @Test
    public void restartCluster() {
        WebSphereService service = null;
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("chvwwwdevcmb01.uschecomrnd.net").setPort("8879").setConnectorType("SOAP").setCluster("wwwdev-trunk-cluster");

            service = new WebSphereService(model);
            service.connect();
            service.restartCluster();
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
        WebSphereService service = null;
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

            service = new WebSphereService(model);
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
        WebSphereService service = null;
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

            service = new WebSphereService(model);
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
