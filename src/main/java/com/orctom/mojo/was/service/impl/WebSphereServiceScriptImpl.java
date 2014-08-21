package com.orctom.mojo.was.service.impl;

import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.model.WebSphereServiceException;
import com.orctom.mojo.was.service.IWebSphereService;
import com.orctom.mojo.was.utils.CommandUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineTimeOutException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;

/**
 * Using jython
 * Created by CH on 3/11/14.
 */
public class WebSphereServiceScriptImpl implements IWebSphereService {

    private WebSphereModel model;
    private String workingDir;

    private static final String TEMPLATE = "jython/websphere.py";
    private static final String TEMPLATE_EXT = "py";

    public WebSphereServiceScriptImpl(WebSphereModel model, String targetDir) {
        System.out.println("Using Jython");
        this.model = model;
        this.workingDir = CommandUtils.getWorkingDir(targetDir, TEMPLATE_EXT);
    }

    public void restartServer() {
        execute("restartServer");
    }

    public void startServer() {
        execute("startServer");
    }

    public void stopServer() {
        execute("stopServer");
    }

    public void installApplication() {
        execute("installApplication");
    }

    public void uninstallApplication() {
        execute("uninstallApplication");
    }

    public void startApplication() {
        execute("startApplication");
    }

    public void stopApplication() {
        execute("stopApplication");
    }

    public void deploy() {
        execute("deploy");
    }

    private void execute(String task) {
        try {
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
            commandLine.createArg().setLine("-tracefile " + buildScript + ".trace");
            commandLine.createArg().setLine("-appendtrace true");
            commandLine.createArg().setLine("-f " + buildScript.getAbsolutePath());
            if (StringUtils.isNotEmpty(model.getScript())) {
                commandLine.createArg().setLine(model.getScriptArgs());
            } else {
                commandLine.createArg().setLine("-o " + task);
            }

            StringStreamConsumer outConsumer = new StringStreamConsumer();
            StringStreamConsumer errConsumer = new StringStreamConsumer();
            CommandUtils.executeCommand(commandLine, outConsumer, errConsumer, model.isVerbose());

            FileUtils.fileWrite(new File(buildScript + ".log"), outConsumer.getOutput());
            String error = errConsumer.getOutput();
            if (StringUtils.isNotEmpty(error)) {
                System.err.println(error);
            }
        } catch (CommandLineTimeOutException e) {
            throw new WebSphereServiceException("Failed to execute task" + task +
                    "\n\tPlease ensure remote WAS or Deployment Manager is running", e);
        } catch (Exception e) {
            throw new WebSphereServiceException("Failed to execute task: " + task, e);
        }
    }

    class StringStreamConsumer implements StreamConsumer {
        private String ls = System.getProperty("line.separator");
        private StringBuffer string = new StringBuffer();

        public void consumeLine(String line) {
            string.append(line).append(ls);
            System.out.println(line);
        }

        public String getOutput() {
            return string.toString();
        }
    }
}
