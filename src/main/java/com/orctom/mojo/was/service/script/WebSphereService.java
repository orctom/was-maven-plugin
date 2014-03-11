package com.orctom.mojo.was.service.script;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.model.WebSphereServiceException;
import com.orctom.mojo.was.service.IWebSphereService;
import org.apache.commons.lang.ArrayUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.*;
import java.util.Vector;

/**
 * Created by CH on 3/11/14.
 */
public class WebSphereService implements IWebSphereService {

    private WebSphereModel model;
    private String workingDir;

    public WebSphereService(WebSphereModel model, String workingDir) {
        this.model = model;
        this.workingDir = workingDir;
    }

    @Override
    public void restartServer() {
        execute("restartServer");
    }

    @Override
    public Vector<String> listApplications() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    @Override
    public void installApplication() {
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
    public boolean isApplicationInstalled() {
        throw new UnsupportedOperationException("not implemented yet");
    }

    private void execute(String task) {
        try {
            Commandline commandline = getCommandline(task);
            executeCommand(commandline);
        } catch (Exception e) {
            throw new WebSphereServiceException("Failed to execute: " + task, e);
        }
    }

    private Commandline getCommandline(String task) throws IOException {
        File buildScript = getBuildScript(task);
        Commandline commandLine = new Commandline();
        commandLine.addEnvironment("WAS_USER_SCRIPT", "");
        commandLine.setExecutable(getWsAntExecutable().getAbsolutePath());
        commandLine.setWorkingDirectory(workingDir);

        commandLine.createArg().setLine("-buildfile " + "\"" + buildScript.getAbsolutePath() + "\"");

        if (model.isVerbose()) {
            commandLine.createArg().setValue("-verbose");
            commandLine.createArg().setValue("-debug");
        }

        return commandLine;
    }

    protected File getWsAntExecutable() {
        File binDir = new File(model.getWasHome(), "bin");
        File[] candidates = binDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                dir.equals(model.getWasHome());
                return name.startsWith("ws_ant");
            }
        });

        if (candidates.length != 1) {
            throw new WebSphereServiceException("Couldn't find ws_ant[.sh|.bat], candidates: " + ArrayUtils.toString(candidates));
        }

        File wsAnt = candidates[0];
        System.out.println("wsAnt location: " + wsAnt.getAbsolutePath());

        return wsAnt;
    }

    private File getBuildScript(String task) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(task + ".xml");

        StringBuilder buildFile = new StringBuilder(50);
        buildFile.append(task)
                .append("-")
                .append(model.getHost())
                .append("-")
                .append(model.getApplicationName())
                .append("-")
                .append(System.currentTimeMillis())
                .append(".xml");

        File buildScriptFile = new File(workingDir + "/was-maven-plugin", buildFile.toString());
        buildScriptFile.mkdirs();
        Writer writer = new FileWriter(buildScriptFile);
        mustache.execute(writer, model).flush();

        return buildScriptFile;
    }

    private void executeCommand(Commandline commandline) {
        try {
            StreamConsumer errConsumer = getStreamConsumer("error");
            StreamConsumer infoConsumer = getStreamConsumer("info");

            if (model.isVerbose()) {
                System.out.println("Executing command line: " + StringUtils.join(commandline.getShellCommandline(), " "));
            }

            int returnCode = CommandLineUtils.executeCommandLine(commandline, infoConsumer, errConsumer);

            String msg = "Return code: " + returnCode;
            if (returnCode != 0) {
                throw new WebSphereServiceException(msg);
            } else {
                System.out.println(msg);
            }
        } catch (CommandLineException e) {
            throw new WebSphereServiceException(e.getMessage());
        }
    }

    private StreamConsumer getStreamConsumer(final String level) {
        StreamConsumer consumer = new StreamConsumer() {
            public void consumeLine(String line) {
                if (level.equalsIgnoreCase("info")) {
                    System.out.println(line);
                } else {
                    System.err.println(line);
                }
            }
        };

        return consumer;
    }
}
