package com.orctom.mojo.was.model;

/**
 * Created by CH on 3/4/14.
 */
public class WebSphereModel {

    private String wasHome;
    private String applicationName;
    private String host;
    private String port;
    private String connectorType;
    private String cluster;
    private String cell;
    private String node;
    private String server;
    private String virtualHost;
    private String user;
    private String password;
    private String contextRoot;
    private String options;
    private String profileName;
    private String packageFile;
    private boolean failOnError;
    private boolean verbose;

    /**
     * Only applicable in "script" mode, set this to have packages and script copied to remote box and execute
     */
    private String remoteWorkingDir;

    private String trustStore;
    private String keyStore;
    private String trustStorePassword;
    private String keyStorePassword;

    public WebSphereModel() {
        wasHome = System.getProperty("WAS_HOME");
        if (null == wasHome) {
            wasHome = System.getenv("WAS_HOME");
        }
    }

    public WebSphereModel(String applicationName, String host, String port, String connectorType, String cluster,
                          String cell, String node, String server, String virtualHost, String user, String password,
                          String contextRoot, String options, String profileName, String packageFile,
                          boolean failOnError, boolean verbose) {
        this.applicationName = applicationName;
        this.host = host;
        this.port = port;
        this.connectorType = connectorType;
        this.cluster = cluster;
        this.cell = cell;
        this.node = node;
        this.server = server;
        this.virtualHost = virtualHost;
        this.user = user;
        this.password = password;
        this.contextRoot = contextRoot;
        this.options = options;
        this.profileName = profileName;
        this.packageFile = packageFile;
        this.failOnError = failOnError;
        this.verbose = verbose;
    }

    public String getWasHome() {
        return wasHome;
    }

    public WebSphereModel setWasHome(String wasHome) {
        if (null != wasHome) {
            this.wasHome = wasHome;
        }
        return this;
    }

    public String getApplicationName() {
        if (null == applicationName && null != packageFile) {
            applicationName = packageFile.substring(packageFile.lastIndexOf("."));
        }
        return applicationName;
    }

    public WebSphereModel setApplicationName(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    public String getHost() {
        return host;
    }

    public WebSphereModel setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        return port;
    }

    public WebSphereModel setPort(String port) {
        this.port = port;
        return this;
    }

    public String getConnectorType() {
        return connectorType;
    }

    public WebSphereModel setConnectorType(String connectorType) {
        this.connectorType = connectorType;
        return this;
    }

    public String getCluster() {
        return cluster;
    }

    public WebSphereModel setCluster(String cluster) {
        this.cluster = cluster;
        return this;
    }

    public String getCell() {
        return cell;
    }

    public WebSphereModel setCell(String cell) {
        this.cell = cell;
        return this;
    }

    public String getNode() {
        return node;
    }

    public WebSphereModel setNode(String node) {
        this.node = node;
        return this;
    }

    public String getServer() {
        return server;
    }

    public WebSphereModel setServer(String server) {
        this.server = server;
        return this;
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public WebSphereModel setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
        return this;
    }

    public String getUser() {
        return user;
    }

    public WebSphereModel setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public WebSphereModel setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public WebSphereModel setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
        return this;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getProfileName() {
        return profileName;
    }

    public WebSphereModel setProfileName(String profileName) {
        this.profileName = profileName;
        return this;
    }

    public String getPackageFile() {
        return packageFile;
    }

    public WebSphereModel setPackageFile(String packageFile) {
        this.packageFile = packageFile;
        return this;
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public WebSphereModel setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public WebSphereModel setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public String getRemoteWorkingDir() {
        return remoteWorkingDir;
    }

    public WebSphereModel setRemoteWorkingDir(String remoteWorkingDir) {
        this.remoteWorkingDir = remoteWorkingDir;
        return this;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public WebSphereModel setTrustStore(String trustStore) {
        this.trustStore = trustStore;
        return this;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public WebSphereModel setKeyStore(String keyStore) {
        this.keyStore = keyStore;
        return this;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public WebSphereModel setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
        return this;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public WebSphereModel setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
        return this;
    }

    @Override
    public String toString() {
        return "WebSphereModel{" +
                "wasHome='" + wasHome + '\'' +
                ", applicationName='" + this.getApplicationName() + '\'' +
                ", host='" + host + '\'' +
                ", port='" + port + '\'' +
                ", connectorType='" + connectorType + '\'' +
                ", cluster='" + cluster + '\'' +
                ", cell='" + cell + '\'' +
                ", node='" + node + '\'' +
                ", server='" + server + '\'' +
                ", virtualHost='" + virtualHost + '\'' +
                ", user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", contextRoot='" + contextRoot + '\'' +
                ", options='" + options + '\'' +
                ", remoteWorkingDir='" + remoteWorkingDir + '\'' +
                ", profileName='" + profileName + '\'' +
                ", packageFile='" + packageFile + '\'' +
                ", failOnError=" + failOnError +
                ", verbose=" + verbose +
                '}';
    }
}
