package com.orctom.mojo.was.service.impl;

import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.model.WebSphereServiceException;
import com.orctom.mojo.was.service.IWebSphereService;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Using jython
 * Created by CH on 3/11/14.
 */
public class WebSphereServiceScriptImpl implements IWebSphereService {

    private WebSphereModel model;
    private String workingDir;

    private static final String TEMPLATE = "jython/websphere.py";
    private static final String TEMPLATE_EXT = ".py";
    private static final String TEMP_DIR = "/was-maven-plugin/py/";

    public WebSphereServiceScriptImpl(WebSphereModel model, String workingDir) {
        this.model = model;
        this.workingDir = workingDir + TEMP_DIR;
    }

    @Override
    public void restartServer() {
        execute("restartServer");
    }

    public void startServer() {
        execute("startServer");
    }

    public void stopServer() {
        execute("stopServer");
    }

    public Collection<String> listApplications() {
        String value = execute("listApplications");
        if (StringUtils.isNotBlank(value)) {
            return Arrays.asList(StringUtils.split(value, " "));
        }
        return null;
    }

    @Override
    public void installApplication() {
        StringBuilder options = new StringBuilder(100);
        //-appname {{applicationName}} -preCompileJSPs true -cluster {{cluster}} -server {{server}}
        options.append("-preCompileJSPs true");
        if (StringUtils.isNotEmpty(model.getApplicationName())) {
            options.append(" -appname ").append(model.getApplicationName());
        }

        if (StringUtils.isNotEmpty(model.getCluster())) {
            options.append(" -cluster ").append(model.getCluster());
        }

        if (StringUtils.isNotEmpty(model.getServer())) {
            options.append(" -server ").append(model.getServer());
        }
        model.setOptions(options.toString());
        execute("installApplication");
    }

    @Override
    public void uninstallApplication() {
        execute("uninstallApplication");
    }

    @Override
    public void startApplication() {
        execute("startApplication");
    }

    @Override
    public void stopApplication() {
        execute("stopApplication");
    }

    @Override
    public void deploy() {
        execute("deploy");
    }

    private String execute(String task) {
        try {
            Commandline commandline = getCommandline(task);

            final StringBuilder rtValue = new StringBuilder(100);
            StreamConsumer outConsumer = new StreamConsumer() {
                boolean isReturnValueLine = false;
                public void consumeLine(String line) {
                    System.out.println(line);
                    if (isReturnValueLine && StringUtils.isBlank(line)) {
                        isReturnValueLine = false;
                    }
                    if (isReturnValueLine) {
                        rtValue.append(line.substring("  [wsadmin] ".length())).append(" ");
                    }
                    if (line.startsWith("  [wsadmin] WAS")) {
                        isReturnValueLine = true;
                    }
                }
            };

            CommandLineUtils.StringStreamConsumer errorConsumer = new CommandLineUtils.StringStreamConsumer();

            CommandUtils.executeCommand(commandline, outConsumer, errorConsumer, model.isVerbose());

            String error = errorConsumer.getOutput();
            if (StringUtils.isNotEmpty(error)) {
                System.out.println(error);
            }

            return rtValue.toString();
        } catch (Exception e) {
            throw new WebSphereServiceException("Failed to execute: " + task, e);
        }
    }

    private Commandline getCommandline(String task) throws IOException {
        File buildScript = CommandUtils.getBuildScript(task, TEMPLATE, model, workingDir, TEMPLATE_EXT);
        Commandline commandLine = new Commandline();
        commandLine.setExecutable(CommandUtils.getExecutable(model.getWasHome(), "wsadmin").getAbsolutePath());
        commandLine.setWorkingDirectory(workingDir);

        commandLine.createArg().setLine("-conntype " + model.getConnectorType());
        commandLine.createArg().setLine("-host " + model.getHost());
        commandLine.createArg().setLine("-port " + model.getPort());
        if (StringUtils.isNotBlank(model.getUser())) {
            commandLine.createArg().setLine("-user " + model.getUser());
            if (StringUtils.isNotBlank(model.getPassword())) {
                commandLine.createArg().setLine("-password " + model.getPassword());
            }
        }
        commandLine.createArg().setLine("-lang jython");
        commandLine.createArg().setLine("-f " + buildScript.getAbsolutePath());
        commandLine.createArg().setLine("-o "  + task);

        return commandLine;
    }
}
