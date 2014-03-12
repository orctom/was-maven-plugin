package com.orctom.mojo.was;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;

/**
 * Created by CH on 3/4/14.
 */
public abstract class AbstractWASMojo extends AbstractMojo {

    @Component
    protected MavenProject project;

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
    private String profileName;

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
}
