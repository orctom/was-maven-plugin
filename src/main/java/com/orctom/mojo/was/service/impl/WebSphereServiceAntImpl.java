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
 * Using WebSphere built-in ant tasks, which will translated to jacl or jython behind the screen
 * Created by CH on 3/11/14.
 */
public class WebSphereServiceAntImpl implements IWebSphereService {

    private WebSphereModel model;
    private String workingDir;

    private static final String TEMPLATE_FOLDER = "ant/";
    private static final String TEMPLATE_EXT = ".xml";

    public WebSphereServiceAntImpl(WebSphereModel model, String workingDir) {
        this.model = model;
        this.workingDir = workingDir + "/was-maven-plugin/ant/";
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
        if (isApplicationInstalled()) {
            uninstallApplication();
        }
        installApplication();
        restartServer();
    }

    public boolean isApplicationInstalled() {
        if (StringUtils.isBlank(model.getApplicationName())) {
            throw new WebSphereServiceException("application name not set");
        }
        Collection<String> applications = listApplications();
        return applications.contains(model.getApplicationName());
    }

    private String execute(String task) {
        try {
            Commandline commandline = getCommandline(task);

            final StringBuilder rtValue = new StringBuilder(100);
            StreamConsumer outConsumer = new StreamConsumer() {
                boolean isReturnValueLine = false;
                boolean rtStop = false;
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
                System.err.println(error);
            }

            return rtValue.toString();
        } catch (Exception e) {
            throw new WebSphereServiceException("Failed to execute: " + task, e);
        }
    }

    private Commandline getCommandline(String task) throws IOException {
        File buildScript = CommandUtils.getBuildScript(task, TEMPLATE_FOLDER + task + TEMPLATE_EXT, model, workingDir, TEMPLATE_EXT);
        Commandline commandLine = new Commandline();
        commandLine.setExecutable(CommandUtils.getExecutable(model.getWasHome(), "ws_ant").getAbsolutePath());
        commandLine.setWorkingDirectory(workingDir);

        commandLine.createArg().setLine("-buildfile " + "\"" + buildScript.getAbsolutePath() + "\"");

        if (model.isVerbose()) {
            commandLine.createArg().setValue("-verbose");
            commandLine.createArg().setValue("-debug");
        }

        return commandLine;
    }
}
