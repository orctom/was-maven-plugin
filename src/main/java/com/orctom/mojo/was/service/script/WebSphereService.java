package com.orctom.mojo.was.service.script;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.model.WebSphereServiceException;
import com.orctom.mojo.was.service.IWebSphereService;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.*;
import java.util.Arrays;
import java.util.Collection;

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
            return executeCommand(commandline);
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
        String wasHome = model.getWasHome();
        if (StringUtils.isBlank(wasHome)) {
            throw new WebSphereServiceException("WAS_HOME is not set");
        }
        File binDir = new File(wasHome, "bin");
        File[] candidates = binDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                dir.equals(model.getWasHome());
                return name.startsWith("ws_ant");
            }
        });

        if (candidates.length != 1) {
            throw new WebSphereServiceException("Couldn't find ws_ant[.sh|.bat], candidates: " + candidates);
        }

        File wsAnt = candidates[0];
        System.out.println("wsAnt location: " + wsAnt.getAbsolutePath());

        return wsAnt;
    }

    private File getBuildScript(String task) throws IOException {
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(task + ".xml");

        StringBuilder buildFile = new StringBuilder(50);
        buildFile.append(task);
        if (StringUtils.isNotBlank(model.getHost())) {
            buildFile.append("-").append(model.getHost());
        }
        if (StringUtils.isNotBlank(model.getApplicationName())) {
            buildFile.append("-").append(model.getApplicationName());
        }
        buildFile.append("-").append(System.currentTimeMillis()).append(".xml");

        File buildScriptFile = new File(workingDir + "/was-maven-plugin", buildFile.toString());
        buildScriptFile.getParentFile().mkdirs();
        Writer writer = new FileWriter(buildScriptFile);
        mustache.execute(writer, model).flush();

        return buildScriptFile;
    }

    private String executeCommand(Commandline commandline) {
        try {
            if (model.isVerbose()) {
                System.out.println("Executing command line: " + StringUtils.join(commandline.getShellCommandline(), " "));
            }

            final StringBuilder rtValue = new StringBuilder(100);
            StreamConsumer consumer = new StreamConsumer() {
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

            CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
            int returnCode = CommandLineUtils.executeCommandLine(commandline, consumer, err);

            String msg = "Return code: " + returnCode;
            if (returnCode != 0) {
                throw new WebSphereServiceException(msg);
            } else {
                System.out.println(msg);
            }

            String error = err.getOutput();
            if (StringUtils.isNotEmpty(error)) {
                System.out.println(error);
            }

            return rtValue.toString();
        } catch (CommandLineException e) {
            throw new WebSphereServiceException(e.getMessage());
        }
    }
}
