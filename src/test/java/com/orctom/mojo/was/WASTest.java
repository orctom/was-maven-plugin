package com.orctom.mojo.was;

import com.orctom.mojo.was.model.Server;
import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.service.WebSphereService;

import java.util.List;

/**
 * Created by CH on 3/4/14.
 */
public class WASTest {

    public static void main(String[] args) {
        try {
            WebSphereModel model = new WebSphereModel();
            model.setHost("chvwwwdevcmb01.uschecomrnd.net").setPort("8886").setConnectorType("SOAP");

            WebSphereService service = new WebSphereService(model);
            service.connect();
            List<Server> servers = service.listServers();
            for (Server server : servers) {
                System.out.println(server.getServerName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
