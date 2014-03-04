package com.orctom.mojo.was.service;

import com.ibm.websphere.management.AdminClient;
import com.ibm.websphere.management.AdminClientFactory;
import com.ibm.websphere.management.application.AppConstants;
import com.ibm.websphere.management.application.AppManagementProxy;
import com.ibm.websphere.management.application.AppNotification;
import com.ibm.websphere.management.application.client.AppDeploymentController;
import com.ibm.websphere.management.application.client.AppDeploymentTask;
import com.ibm.websphere.management.exception.ConnectorException;
import com.orctom.mojo.was.model.Server;
import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.model.WebSphereServiceException;
import org.apache.commons.lang.StringUtils;

import javax.management.*;
import java.util.*;

/**
 * Created by CH on 3/4/14.
 */
public class WebSphereService {

    private AdminClient client;
    private WebSphereModel model;

    public WebSphereService(WebSphereModel model) {
        this.model = model;
    }

    @SuppressWarnings("unchecked")
    public List<Server> listServers() {
        try {
            ObjectName jvmQuery = new ObjectName("WebSphere:*,type=Server");
            Set<ObjectName> response = client.queryNames(jvmQuery, null);
            List<Server> servers = new ArrayList<Server>();
            for (ObjectName serverObjectName : response) {
                Server server = new Server();
                server.setCellName(String.valueOf(client.getAttribute(serverObjectName, "cellName")));
                server.setNodeName(String.valueOf(client.getAttribute(serverObjectName, "nodeName")));
                server.setServerName(String.valueOf(client.getAttribute(serverObjectName, "name")));
                server.setProcessId(String.valueOf(client.getAttribute(serverObjectName, "pid")));
                server.setServerVendor(String.valueOf(client.getAttribute(serverObjectName, "serverVendor")));
                server.setServerVersion(String.valueOf(client.getAttribute(serverObjectName, "serverVersion")));
                servers.add(server);
            }
            return servers;
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebSphereServiceException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public void installApplication(HashMap<String, Object> options) {
        if (!isConnected()) {
            throw new WebSphereServiceException("Cannot install artifact, no connection to WebSphere Application Server exists");
        }
        try {
            Hashtable<String, Object> preferences = new Hashtable<String, Object>();
            preferences.put(AppConstants.APPDEPL_LOCALE, Locale.getDefault());

            Properties defaultBinding = new Properties();
            preferences.put(AppConstants.APPDEPL_DFLTBNDG, defaultBinding);
            if (options.containsKey(AppConstants.APPDEPL_DFLTBNDG_VHOST)) {
                defaultBinding.put(AppConstants.APPDEPL_DFLTBNDG_VHOST, options.get(AppConstants.APPDEPL_DFLTBNDG_VHOST));
            }

            AppDeploymentController controller = AppDeploymentController.readArchive(model.getPackageFile().getAbsolutePath(), preferences);

            AppDeploymentTask task = controller.getFirstTask();
            while (task != null) {
                String[][] data = task.getTaskData();
                task.setTaskData(data);
                task = controller.getNextTask();
            }
            controller.saveAndClose();

            Hashtable<String, Object> config = controller.getAppDeploymentSavedResults();

            config.put(AppConstants.APPDEPL_LOCALE, Locale.getDefault());
            config.put(AppConstants.APPDEPL_ARCHIVE_UPLOAD, Boolean.TRUE);
            config.put(AppConstants.APPDEPL_PRECOMPILE_JSP, Boolean.TRUE);

            Hashtable<String, Object> module2server = new Hashtable<String, Object>();
            module2server.put("*", getTarget());
            config.put(AppConstants.APPDEPL_MODULE_TO_SERVER, module2server);

            InstallationListener listener = createInstallationListener();
            client.addNotificationListener(listener.getAppManagement(), listener, listener.getFilter(), "");
            AppManagementProxy.getJMXProxyForClient(client).installApplication(model.getPackageFile().getAbsolutePath(), config, null);
            waitForInstallThread();
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebSphereServiceException("Failed to install artifact: " + e.getMessage());
        }
    }

    public void uninstallApplication(String name) throws Exception {
        InstallationListener listener = createInstallationListener();

        client.addNotificationListener(listener.getAppManagement(), listener, listener.getFilter(), "");

        AppManagementProxy.getJMXProxyForClient(client).uninstallApplication(name, new Hashtable(), null);

        waitForInstallThread();
    }

    public void startApplication(String name) throws Exception {
        try {
            AppManagementProxy.getJMXProxyForClient(client).startApplication(name, new Hashtable(), null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebSphereServiceException("Could not start artifact '" + name + "': " + e.getMessage());
        }
    }

    public void stopApplication(String name) throws Exception {
        try {
            AppManagementProxy.getJMXProxyForClient(client).stopApplication(name, new Hashtable(), null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebSphereServiceException("Could not stop artifact '" + name + "': " + e.getMessage());
        }
    }

    public boolean isApplicationInstalled(String name) {
        try {
            return AppManagementProxy.getJMXProxyForClient(client).checkIfAppExists(name, new Hashtable(), null);
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebSphereServiceException("Could not determine if artifact '" + name + "' is installed: " + e.getMessage());
        }
    }

    private InstallationListener createInstallationListener() throws Exception {
        NotificationFilterSupport filter = new NotificationFilterSupport();
        filter.enableType(AppConstants.NotificationType);
        return new InstallationListener(client, getAppManagementObject(), filter);
    }

    public boolean isConnected() {
        try {
            return null != client && null != client.isAlive();
        } catch (Exception e) {
            return false;
        }
    }

    public void connect() throws Exception {
        if (isConnected()) {
            System.out.println("WARNING: Already connected to WebSphere Application Server");
        }
        Properties config = new Properties();
        config.put(AdminClient.CONNECTOR_HOST, model.getHost());
        config.put(AdminClient.CONNECTOR_PORT, model.getPort());
        config.put(AdminClient.CACHE_DISABLED, "false");
        if (StringUtils.isNotEmpty(model.getUser())) {
            injectSecurityConfiguration(config);
        } else {
            config.put(AdminClient.CONNECTOR_SECURITY_ENABLED, "false");
        }

        config.put(AdminClient.AUTH_TARGET, getTarget());
        config.put(AdminClient.CONNECTOR_TYPE, model.getConnectorType());
        client = AdminClientFactory.createAdminClient(config);
        if (client == null) {
            throw new WebSphereServiceException("Unable to connect to IBM WebSphere Application Server @ " + model.getHost() + ":" + model.getPort());
        }
    }

    private String getTarget() {
        StringBuilder builder = new StringBuilder();
        builder.append("WebSphere:");
        appendTarget(builder, "cluster=", model.getCluster());
        appendTarget(builder, "cell=", model.getCell());
        appendTarget(builder, "node=", model.getNode());
        appendTarget(builder, "server=", model.getServer());
        return builder.toString();
    }

    private void appendTarget(StringBuilder builder, String target, String value) {
        if (StringUtils.isNotEmpty(value)) {
            if (':' != builder.charAt(builder.length() - 1)) {
                builder.append(",");
            }
            builder.append(target);
            builder.append(value);
        }
    }

    private void injectSecurityConfiguration(Properties config) {
        config.put(AdminClient.CONNECTOR_SECURITY_ENABLED, "true");
        config.put(AdminClient.USERNAME, model.getUser());
        config.put(AdminClient.PASSWORD, model.getPassword());

        config.put("com.ibm.ssl.trustStore", model.getTrustStoreLocation().getAbsolutePath());
        config.put("javax.net.ssl.trustStore", model.getTrustStoreLocation().getAbsolutePath());

        config.put("com.ibm.ssl.keyStore", model.getKeyStoreLocation().getAbsolutePath());
        config.put("javax.net.ssl.keyStore", model.getKeyStoreLocation().getAbsolutePath());

        config.put("com.ibm.ssl.trustStorePassword", model.getTrustStorePassword());
        config.put("javax.net.ssl.trustStorePassword", model.getTrustStorePassword());

        config.put("com.ibm.ssl.keyStorePassword", model.getKeyStorePassword());
        config.put("javax.net.ssl.keyStorePassword", model.getKeyStorePassword());
    }

    public void disconnect() {
        client = null;
    }

    public boolean isAvailable() {
        try {
            Class.forName("com.ibm.websphere.management.AdminClientFactory", false, getClass().getClassLoader());
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    private ObjectName getAppManagementObject() throws MalformedObjectNameException, ConnectorException {
        //only one app management object exists for WebSphere so return the first one
        Iterator iterator = client.queryNames(new ObjectName("WebSphere:type=AppManagement,*"), null).iterator();
        return (ObjectName) iterator.next();
    }


    private void waitForInstallThread() {
        synchronized (this) {
            try {
                wait();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyInstallThread() {
        synchronized (this) {
            notify();
        }
    }

    class InstallationListener implements NotificationListener {

        private AdminClient client;
        private ObjectName appManagement;
        private NotificationFilter filter;

        public InstallationListener(AdminClient adminClient, ObjectName appManagement, NotificationFilter filter) {
            this.client = adminClient;
            this.appManagement = appManagement;
            this.filter = filter;
        }

        public synchronized void handleNotification(Notification notification, Object handback) {
            AppNotification ev = (AppNotification) notification.getUserData();

            if (ev.taskName.equals(AppNotification.INSTALL) && (ev.taskStatus.equals(AppNotification.STATUS_COMPLETED) || ev.taskStatus.equals(AppNotification.STATUS_FAILED))) {
                try {
                    client.removeNotificationListener(appManagement, this);
                } catch (Throwable th) {
                    System.err.println("Error removing install listener: " + th);
                }
                notifyInstallThread();
            }

            if (ev.taskName.equals(AppNotification.UNINSTALL) && (ev.taskStatus.equals(AppNotification.STATUS_COMPLETED) || ev.taskStatus.equals(AppNotification.STATUS_FAILED))) {
                try {
                    client.removeNotificationListener(appManagement, this);
                } catch (Throwable th) {
                    System.err.println("Error removing uninstall listener: " + th);
                }
                notifyInstallThread();
            }
        }

        public NotificationFilter getFilter() {
            return filter;
        }

        public AdminClient getClient() {
            return client;
        }

        public ObjectName getAppManagement() {
            return appManagement;
        }
    }

}
