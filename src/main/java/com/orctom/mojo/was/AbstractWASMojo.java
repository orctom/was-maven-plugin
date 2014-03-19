package com.orctom.mojo.was;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.orctom.mojo.was.model.Meta;
import com.orctom.mojo.was.model.WebSphereModel;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.PropertyUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.*;
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

    @Parameter(defaultValue = "SCRIPT")
    protected String mode;

    @Parameter(defaultValue = "${project.basedir}/deploy-target-servers.properties")
    protected File deploymentsPropertyFile;

    @Parameter
    protected String wasHome;

    @Parameter(defaultValue = "${project.build.finalName}")
    protected String applicationName;

    @Parameter(defaultValue = "localhost")
    protected String host;

    @Parameter(defaultValue = "8879")
    protected String port;

    @Parameter(defaultValue = "SOAP")
    protected String connectorType;

    @Parameter
    protected String cluster;

    @Parameter
    protected String cell;

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

    @Parameter
    protected String remoteWorkingDir;

    @Parameter(defaultValue = "${project.artifact.file}")
    protected File packageFile;

    @Parameter(defaultValue = "true")
    protected boolean failOnError;

    @Parameter
    protected boolean verbose;

    @Parameter
    protected String trustStore;

    @Parameter
    protected String keyStore;

    @Parameter
    protected String trustStorePassword;

    @Parameter
    protected String keyStorePassword;

    /**
     * The XML for the Ant target.
     */
    @Parameter
    protected PlexusConfiguration[] preSteps;

    /**
     * The XML for the Ant target.
     */
    @Parameter
    protected PlexusConfiguration[] postSteps;

    protected Set<WebSphereModel> getWebSphereModels() {
        if (null != deploymentsPropertyFile && deploymentsPropertyFile.exists()) {
            Properties properties = PropertyUtils.loadProperties(deploymentsPropertyFile);
            project.getProperties().putAll(properties);
        }

        String deployTargets = System.getProperty(Constants.KEY_DEPLOY_TARGETS);

        if (StringUtils.isNotBlank(deployTargets)) {
            return getWebSphereModels(deployTargets);
        } else {
            WebSphereModel model = getWebSphereModel();
            if (!model.isValid()) {
                return null;
            }
            Set<WebSphereModel> models = new HashSet<>(1);
            models.add(model);
            return models;
        }
    }

    protected WebSphereModel getWebSphereModel() {
        Meta meta = new Meta()
                .setBrand(getPropertyValue("meta.brand"))
                .setLocale(getPropertyValue("meta.locale"))
                .setCdap(getPropertyValue("meta.cdap"));
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
                .setRemoteWorkingDir(remoteWorkingDir)
                .setTrustStore(trustStore)
                .setTrustStorePassword(trustStorePassword)
                .setKeyStore(keyStore)
                .setKeyStorePassword(keyStorePassword)
                .setFailOnError(failOnError)
                .setVerbose(verbose)
                .setMeta(meta);
    }

    protected Set<WebSphereModel> getWebSphereModels(String deployTargetStr) {
        Set<String> deployTargets = new HashSet<>();
        Collections.addAll(deployTargets, StringUtils.split(deployTargetStr, ","));

        Set<WebSphereModel> models = new HashSet<>();
        for (String deployTarget : deployTargets) {
            Meta meta = new Meta()
                    .setBrand(getPropertyValue("meta.brand"))
                    .setBrand(getPropertyValue(deployTarget + "meta.brand"))
                    .setLocale(getPropertyValue("meta.locale"))
                    .setLocale(getPropertyValue(deployTarget + "meta.locale"))
                    .setCdap(getPropertyValue("meta.cdap"))
                    .setCdap(getPropertyValue(deployTarget + "meta.cdap"));
            WebSphereModel model = new WebSphereModel()
                    .setWasHome(wasHome)
                    .setApplicationName(getPropertyValue(deployTarget + ".applicationName"))
                    .setHost(getPropertyValue(deployTarget + ".host"))
                    .setPort(getPropertyValue(deployTarget + ".port"))
                    .setConnectorType(getPropertyValue(deployTarget + ".connectorType"))
                    .setCluster(getPropertyValue(deployTarget + ".cluster"))
                    .setCell(getPropertyValue(deployTarget + ".cell"))
                    .setNode(getPropertyValue(deployTarget + ".node"))
                    .setServer(getPropertyValue(deployTarget + ".server"))
                    .setVirtualHost(getPropertyValue(deployTarget + ".virtualHost"))
                    .setContextRoot(getPropertyValue(deployTarget + ".contextRoot"))
                    .setUser(getPropertyValue(deployTarget + ".user"))
                    .setPassword(getPropertyValue(deployTarget + ".password"))
                    .setProfileName(profileName)
                    .setPackageFile(packageFile.getAbsolutePath())
                    .setRemoteWorkingDir(remoteWorkingDir)
                    .setTrustStore(trustStore)
                    .setTrustStorePassword(trustStorePassword)
                    .setKeyStore(keyStore)
                    .setKeyStorePassword(keyStorePassword)
                    .setFailOnError(failOnError)
                    .setVerbose(verbose)
                    .setMeta(meta);
            if (model.isValid()) {
                models.add(model);
            }
        }

        return models;
    }

    protected String getPropertyValue(String propertyName) {
        String value = project.getProperties().getProperty(propertyName);
        if (null != value && value.contains("{{") && value.contains("}}")) {
            System.out.println("1 " + value);
            System.out.println("2 " + project.getProperties().getProperty("meta.brand"));
            System.out.println("3 " + project.getProperties().getProperty("meta.locale"));
            System.out.println("4 " + project.getProperties().getProperty("meta.cdap"));
            for (Map.Entry<Object, Object> entry : project.getProperties().entrySet()) {
                System.out.println(entry.getKey() + " - " + entry.getValue());
            }
            System.out.println("-----------");
            MustacheFactory mf = new DefaultMustacheFactory();
            Mustache mustache = mf.compile(new StringReader(value), "dummy");
            StringWriter writer = new StringWriter();
            try {
                mustache.execute(writer, project.getProperties()).flush();
                value = writer.toString();
                System.out.println(propertyName + " = " + value);
                if (StringUtils.isNotEmpty(value)) {
                    project.getProperties().setProperty(propertyName, value);
                }
            } catch (IOException e) {
                System.err.println("Failed to resolve property: " + propertyName + ", "+ e.getMessage());
                return project.getProperties().getProperty(propertyName);
            }
        }
        return value;
    }
}
