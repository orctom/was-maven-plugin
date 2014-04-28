package com.orctom.mojo.was;

import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.utils.PropertiesUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.tools.ant.taskdefs.PathConvert;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.util.*;

import static com.orctom.mojo.was.utils.PropertiesUtils.SectionedProperties.DEFAULT_SECTION;

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

    @Parameter(defaultValue = "SCRIPT")
    protected String mode;

    @Parameter(defaultValue = "${project.basedir}/was-maven-plugin.properties")
    protected File deploymentsPropertyFile;

    @Parameter(required = true)
    protected String wasHome;

    @Parameter(defaultValue = "${project.build.finalName}")
    protected String applicationName;

    @Parameter(defaultValue = "localhost")
    protected String host;

    @Parameter(defaultValue = "8879")
    protected String port;

    @Parameter(defaultValue = "SOAP")
    protected String connectorType;

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

    @Parameter(defaultValue = "AppSrv01")
    protected String profileName;

    @Parameter(defaultValue = "${project.artifact.file}")
    protected File packageFile;

    @Parameter(defaultValue = "false")
    protected boolean failOnError;

    @Parameter
    protected String script;

    @Parameter
    protected String scriptArgs;

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
                Map<String, Properties> propertiesMap = PropertiesUtils.loadSectionedProperties(deploymentsPropertyFile, DEFAULT_SECTION, project.getProperties());
                if (propertiesMap.size() >= 1) {
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
            Set<WebSphereModel> models = new HashSet<>(1);
            models.add(model);
            return models;
        }
    }

    protected WebSphereModel getWebSphereModel() {
        return new WebSphereModel()
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
                .setUser(user)
                .setPassword(password)
                .setProfileName(profileName)
                .setPackageFile(packageFile.getAbsolutePath())
                .setFailOnError(failOnError)
                .setVerbose(verbose);
    }

    protected Set<WebSphereModel> getWebSphereModels(String deployTargetStr, Map<String, Properties> propertiesMap) {
        Set<String> deployTargets = new HashSet<>();
        Collections.addAll(deployTargets, StringUtils.split(deployTargetStr, ","));

        Set<WebSphereModel> models = new HashSet<>();
        for (String deployTarget : deployTargets) {
            Properties props = propertiesMap.get(deployTarget);
            if (null == props || props.isEmpty()) {
                getLog().info("[SKIPPED] " + deployTarget + ", not configured in property file.");
                continue;
            }
            String appName = applicationName;
            String appNameSuffix = getPropertyValue("applicationNameSuffix", props);
            if (StringUtils.isNotEmpty(appNameSuffix)) {
                appName = appName + "_" + appNameSuffix;
            }
            WebSphereModel model = new WebSphereModel()
                    .setWasHome(wasHome)
                    .setApplicationName(appName)
                    .setHost(getPropertyValue("host", props))
                    .setPort(getPropertyValue("port", props))
                    .setConnectorType(getPropertyValue("connectorType", props))
                    .setCluster(getPropertyValue("cluster", props))
                    .setCell(getPropertyValue("cell", props))
                    .setNode(getPropertyValue("node", props))
                    .setServer(getPropertyValue("server", props))
                    .setVirtualHost(getPropertyValue("virtualHost", props))
                    .setContextRoot(getPropertyValue("contextRoot", props))
                    .setUser(getPropertyValue("user", props))
                    .setPassword(getPropertyValue("password", props))
                    .setProfileName(profileName)
                    .setPackageFile(packageFile.getAbsolutePath())
                    .setFailOnError(failOnError)
                    .setVerbose(verbose);
            model.setProperties(props);
            if (model.isValid()) {
                models.add(model);
                props.setProperty("applicationName", model.getApplicationName());
            }
        }

        return models;
    }

    protected String getPropertyValue(String propertyName, Properties props) {
        String value = props.getProperty(propertyName);
        if (null != value && value.contains("{{") && value.contains("}}")) {
            value = PropertiesUtils.resolve(value, props);
            if (StringUtils.isNotEmpty(value)) {
                props.setProperty(propertyName, value);
            }
        }
        return value;
    }
}
