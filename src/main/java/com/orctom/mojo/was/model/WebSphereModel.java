package com.orctom.mojo.was.model;

import java.io.File;

/**
 * Created by CH on 3/4/14.
 */
public class WebSphereModel {

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
    private File packageFile;
    private boolean failOnError;
    private boolean verbose;
    private int retryCounts;

    private File trustStoreLocation;
    private File keyStoreLocation;
    private String trustStorePassword;
    private String keyStorePassword;

    public WebSphereModel() {
    }

    public WebSphereModel(String applicationName, String host, String port, String connectorType, String cluster,
                          String cell, String node, String server, String virtualHost, String user, String password,
                          String contextRoot, File packageFile, boolean failOnError, boolean verbose, int retryCounts) {
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
        this.packageFile = packageFile;
        this.failOnError = failOnError;
        this.verbose = verbose;
        this.retryCounts = retryCounts;
    }

    public String getApplicationName() {
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

    public File getPackageFile() {
        return packageFile;
    }

    public WebSphereModel setPackageFile(File packageFile) {
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

    public int getRetryCounts() {
        return retryCounts;
    }

    public WebSphereModel setRetryCounts(int retryCounts) {
        this.retryCounts = retryCounts;
        return this;
    }

    public File getTrustStoreLocation() {
        return trustStoreLocation;
    }

    public WebSphereModel setTrustStoreLocation(File trustStoreLocation) {
        this.trustStoreLocation = trustStoreLocation;
        return this;
    }

    public File getKeyStoreLocation() {
        return keyStoreLocation;
    }

    public WebSphereModel setKeyStoreLocation(File keyStoreLocation) {
        this.keyStoreLocation = keyStoreLocation;
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
}
