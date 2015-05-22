package com.orctom.mojo.was;

import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.utils.PropertiesUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.*;

/**
 * Abstract Mojo for websphere deployment
 * Created by CH on 3/4/14.
 */
public abstract class AbstractWASMojo extends AbstractMojo {

    @Component
    protected MavenProject project;

    @Component
    protected MavenProjectHelper projectHelper;

    @Parameter(defaultValue = "${plugin.artifacts}")
    protected List<Artifact> pluginArtifacts;

    @Parameter(defaultValue = "${project.basedir}/was-maven-plugin.properties")
    protected File deploymentsPropertyFile;

    @Parameter(required = true)
    protected String wasHome;

    @Parameter(defaultValue = "${project.build.finalName}")
    protected String applicationName;

    @Parameter(defaultValue = "localhost")
    protected String host;

    @Parameter
    protected String port;

    @Parameter
    protected String connectorType;

    @Parameter(defaultValue = "true")
    protected boolean restartAfterDeploy;

    /**
     * Required if target server is a cluster
     */
    @Parameter
    protected String cluster;

    @Parameter
    protected String cell;

    /**
     * Required if target server is NOT cluster
     */
    @Parameter
    protected String node;

    @Parameter
    protected String server;

    @Parameter
    protected String virtualHost;

    @Parameter
    protected String user;

    @Parameter
    protected String password;

    @Parameter
    protected String contextRoot;

    @Parameter
    protected String sharedLibs;

    @Parameter
    protected boolean parentLast;

    @Parameter
    protected boolean webModuleParentLast;

    @Parameter(defaultValue = "${project.artifact.file}")
    protected File packageFile;

    @Parameter(defaultValue = "false")
    protected boolean failOnError;

    @Parameter
    protected String script;

    @Parameter
    protected String scriptArgs;
    
    @Parameter
    protected String javaoption;

    @Parameter
    protected boolean verbose;

    /**
     * The XML for the Ant target.
     */
    @Parameter
    protected PlexusConfiguration[] preSteps;

    /**
     * The XML for the Ant target
     */
    @Parameter
    protected PlexusConfiguration[] postSteps;

    protected Set<WebSphereModel> getWebSphereModels() {
        String deployTargets = System.getProperty(Constants.KEY_DEPLOY_TARGETS);

        if (StringUtils.isNotBlank(deployTargets)) {
            if (null != deploymentsPropertyFile && deploymentsPropertyFile.exists()) {
                project.getBasedir();
                Map<String, Properties> propertiesMap = PropertiesUtils.loadSectionedProperties(deploymentsPropertyFile, getProjectProperties());
                if (null != propertiesMap && propertiesMap.size() >= 1) {
                    getLog().info("Multi targets: " + deployTargets);
                    return getWebSphereModels(deployTargets, propertiesMap);
                }
            }

            if (null == deploymentsPropertyFile) {
                getLog().info("Property config file: " + deploymentsPropertyFile + " not configured.");
            }
            if (!deploymentsPropertyFile.exists()) {
                getLog().info("Property config file: " + deploymentsPropertyFile + " doesn't exist.");
            }
            getLog().info("Single target not properly configured.");
            return null;
        } else {
            WebSphereModel model = getWebSphereModel();
            if (!model.isValid()) {
                getLog().info("Single target not properly configured. Missing 'cell' or 'cluster' or 'server' or 'node'");
                return null;
            }
            getLog().info("Single target: " + model.getHost());
            Set<WebSphereModel> models = new HashSet<WebSphereModel>(1);
            models.add(model);
            return models;
        }
    }

    protected WebSphereModel getWebSphereModel() {
    	WebSphereModel model = new WebSphereModel()
                .setWasHome(wasHome)
                .setApplicationName(applicationName)
                .setHost(host)
                .setPort(port)
                .setConnectorType(connectorType)
                .setCluster(cluster)
                .setCell(cell)
                .setNode(node)
                .setServer(server)
                .setVirtualHost(virtualHost)
                .setContextRoot(contextRoot)
                .setSharedLibs(sharedLibs)
                .setParentLast(parentLast)
                .setWebModuleParentLast(webModuleParentLast)
                .setUser(user)
                .setPassword(password)
                .setPackageFile(packageFile.getAbsolutePath())
                .setScript(script)
                .setScriptArgs(scriptArgs)
                .setJavaoption(javaoption)
                .setFailOnError(failOnError)
                .setRestartAfterDeploy(restartAfterDeploy)
                .setVerbose(verbose);
    	
    	model.setProperties(getProjectProperties());
    	
    	return model;
    }

    protected Set<WebSphereModel> getWebSphereModels(String deployTargetStr, Map<String, Properties> propertiesMap) {
        Set<String> deployTargets = new HashSet<String>();
        Collections.addAll(deployTargets, StringUtils.split(deployTargetStr, ","));

        Set<WebSphereModel> models = new HashSet<WebSphereModel>();
        for (String deployTarget : deployTargets) {
            Properties props = propertiesMap.get(deployTarget);
            if (null == props || props.isEmpty()) {
                getLog().info("[SKIPPED] " + deployTarget + ", not configured in property file.");
                continue;
            }
            
            updateApplicationNameWithSuffix(props);
            
            WebSphereModel model = new WebSphereModel()
                    .setWasHome(wasHome)
                    .setApplicationName(getPropertyValue("applicationName", props))
                    .setHost(getPropertyValue("host", props))
                    .setPort(getPropertyValue("port", props))
                    .setConnectorType(getPropertyValue("connectorType", props))
                    .setCluster(getPropertyValue("cluster", props))
                    .setCell(getPropertyValue("cell", props))
                    .setNode(getPropertyValue("node", props))
                    .setServer(getPropertyValue("server", props))
                    .setVirtualHost(getPropertyValue("virtualHost", props))
                    .setContextRoot(getPropertyValue("contextRoot", props))
                    .setSharedLibs(getPropertyValue("sharedLibs", props))
                    .setParentLast(Boolean.valueOf(getPropertyValue("parentLast", props)))
                    .setWebModuleParentLast(Boolean.valueOf(getPropertyValue("webModuleParentLast", props)))
                    .setUser(getPropertyValue("user", props))
                    .setPassword(getPropertyValue("password", props))
                    .setPackageFile(packageFile.getAbsolutePath())
                    .setScript(script)
                    .setScriptArgs(scriptArgs)
                    .setJavaoption(javaoption)
                    .setFailOnError(failOnError)
                    .setRestartAfterDeploy(Boolean.valueOf(getPropertyValue("restartAfterDeploy", props)))
                    .setVerbose(verbose);

            model.setProperties(props);
            if (model.isValid()) {
                models.add(model);
            }
        }

        return models;
    }
    
    private void updateApplicationNameWithSuffix(Properties props) {
        String appNameSuffix = getPropertyValue("applicationNameSuffix", props);
        if (StringUtils.isNotEmpty(appNameSuffix)) {
        	String appName = getPropertyValue("applicationName", props);
        	props.setProperty("applicationName", appName + "_" + appNameSuffix);
        }
    }

    protected String getPropertyValue(String propertyName, Properties props) {
        String value = props.getProperty(propertyName);
        if (isValueNotResolved(value)) {
            value = PropertiesUtils.resolve(value, props);
            props.setProperty(propertyName, value);
        }
        return value;
    }

    private boolean isValueNotResolved(String value) {
        return StringUtils.isNotEmpty(value) && value.contains("{{") && value.contains("}}");
    }

    private Properties getProjectProperties() {
        Properties properties = new Properties(project.getProperties());
        setProperty(properties, "applicationName", applicationName);
        setProperty(properties, "host", host);
        setProperty(properties, "port", port);
        setProperty(properties, "connectorType", connectorType);
        setProperty(properties, "cluster", cluster);
        setProperty(properties, "cell", cell);
        setProperty(properties, "node", node);
        setProperty(properties, "server", server);
        setProperty(properties, "virtualHost", virtualHost);
        setProperty(properties, "user", user);
        setProperty(properties, "password", password);
        setProperty(properties, "contextRoot", contextRoot);
        setProperty(properties, "sharedLibs", sharedLibs);
        setProperty(properties, "parentLast", String.valueOf(parentLast));
        setProperty(properties, "webModuleParentLast", String.valueOf(webModuleParentLast));
        setProperty(properties, "packageFile", packageFile.getAbsolutePath());
        setProperty(properties, "javaoption", javaoption);
        setProperty(properties, "failOnError", String.valueOf(failOnError));
        setProperty(properties, "script", script);
        setProperty(properties, "scriptArgs", scriptArgs);
        setProperty(properties, "verbose", String.valueOf(verbose));
        setProperty(properties, "restartAfterDeploy", String.valueOf(restartAfterDeploy));

        properties.setProperty("basedir", project.getBasedir().getAbsolutePath());
        properties.setProperty("project.basedir", project.getBasedir().getAbsolutePath());
        properties.setProperty("version", project.getVersion());
        properties.setProperty("project.version", project.getVersion());
        properties.setProperty("project.build.directory", project.getBuild().getDirectory());
        properties.setProperty("project.build.outputDirectory", project.getBuild().getOutputDirectory());
        properties.setProperty("project.build.finalName", project.getBuild().getFinalName());
        properties.setProperty("project.name", project.getName());
        properties.setProperty("groupId", project.getGroupId());
        properties.setProperty("project.groupId", project.getGroupId());
        properties.setProperty("artifactId", project.getArtifactId());
        properties.setProperty("project.artifactId", project.getArtifactId());
        return properties;
    }

    private void setProperty(Properties properties, String key, String value) {
        if (StringUtils.isNotEmpty(value)) {
            properties.setProperty(key, value);
        }
    }
}
