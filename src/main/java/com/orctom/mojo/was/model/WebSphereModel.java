package com.orctom.mojo.was.model;

import org.codehaus.plexus.util.StringUtils;

import java.util.Properties;

/**
 * Created by CH on 3/4/14.
 */
public class WebSphereModel {

    private String wasHome;
    private String applicationName;
    private String host;
    private String port = "8880";
    private String connectorType = "SOAP";
    private String cluster;
    private String cell;
    private String node;
    private String server;
    private String virtualHost;
    private String user;
    private String password;
    private String contextRoot;
    private String sharedLibs;
    private boolean parentLast;
    private boolean restartAfterDeploy;
    private boolean webModuleParentLast;
    private String packageFile;
    private String script;
    private String scriptArgs;
    private String javaoption;
    private boolean failOnError;
    private boolean verbose;

    private Properties properties;

    public WebSphereModel() {
        wasHome = System.getProperty("WAS_HOME");
        if (null == wasHome) {
            wasHome = System.getenv("WAS_HOME");
        }
    }

    public String getWasHome() {
        return wasHome;
    }

    public WebSphereModel setWasHome(String wasHome) {
        if (StringUtils.isNotBlank(wasHome)) {
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
        if (null == port) {
            if (null != cluster) {
                port = "8879";
            } else {
                port = "8880";
            }
        }
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
        if (StringUtils.isNotBlank(connectorType)) {
            this.connectorType = connectorType;
        }
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

    public String getSharedLibs() {
        return sharedLibs;
    }

    public WebSphereModel setSharedLibs(String sharedLibs) {
        this.sharedLibs = sharedLibs;
        return this;
    }

    public boolean isParentLast() {
        return parentLast;
    }

    public WebSphereModel setParentLast(boolean parentLast) {
        this.parentLast = parentLast;
        return this;
    }

    public boolean isWebModuleParentLast() {
        return webModuleParentLast;
    }

    public WebSphereModel setWebModuleParentLast(boolean webModuleParentLast) {
        this.webModuleParentLast = webModuleParentLast;
        return this;
    }

    public String getPackageFile() {
        return packageFile;
    }

    public WebSphereModel setPackageFile(String packageFile) {
        this.packageFile = packageFile;
        return this;
    }

    public String getScript() {
        return script;
    }

    public WebSphereModel setScript(String script) {
        this.script = script;
        return this;
    }

    public String getScriptArgs() {
        return scriptArgs;
    }

    public WebSphereModel setScriptArgs(String scriptArgs) {
        this.scriptArgs = scriptArgs;
        return this;
    }

    public String getJavaoption() {
        return javaoption;
    }

    public WebSphereModel setJavaoption(String javaoption) {
        this.javaoption = javaoption;
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

    public boolean shouldRestartAfterDeploy() {
        return restartAfterDeploy;
    }

    public WebSphereModel setRestartAfterDeploy(boolean restartAfterDeploy) {
        this.restartAfterDeploy = restartAfterDeploy;
        return this;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "WebSphereModel{" +
                "wasHome='" + wasHome + '\'' +
                ", applicationName='" + applicationName + '\'' +
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
                ", sharedLibs='" + sharedLibs + '\'' +
                ", parentLast='" + parentLast + '\'' +
                ", webModuleParentLast='" + webModuleParentLast + '\'' +
                ", packageFile='" + packageFile + '\'' +
                ", script='" + script + '\'' +
                ", scriptArgs='" + scriptArgs + '\'' +
                ", javaoption='" + javaoption + '\'' +
                ", failOnError=" + failOnError +
                ", verbose=" + verbose +
                ", restartAfterDeploy='" + restartAfterDeploy + '\'' +
                ", properties=" + properties +
                '}';
    }

    public boolean isValid() {
        return (StringUtils.isNotBlank(cluster) ||
                StringUtils.isNotBlank(cell) ||
                StringUtils.isNotBlank(server) ||
                StringUtils.isNotBlank(node));
    }
}
