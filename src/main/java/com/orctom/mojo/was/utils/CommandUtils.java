package com.orctom.mojo.was.utils;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.orctom.mojo.was.Constants;
import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.model.WebSphereServiceException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * Command Utils for executing command line commands or scripts
 * Created by CH on 3/13/14.
 */
public class CommandUtils {

    private static final String TIMESTAMP_FORMAT = "yyyyMMdd-HHmmss-SSS";

    public static String getWorkingDir(String targetFolder, String templateExt) {
        return targetFolder + File.separator + Constants.PLUGIN_ID + File.separator + templateExt + File.separator;
    }

    public static File getExecutable(final String wasHome, final String name) {
        if (StringUtils.isBlank(wasHome)) {
            throw new WebSphereServiceException("WAS_HOME is not set");
        }
        File binDir = new File(wasHome, "bin");
        File[] candidates = binDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String fileName) {
                return fileName.startsWith(name) && (fileName.endsWith("bat") || fileName.endsWith("sh"));
            }
        });

        if (null == candidates) {
            throw new WebSphereServiceException("Wrong wasHome or WAS_HOME: " + wasHome);
        }

        if (candidates.length != 1) {
            throw new WebSphereServiceException("Couldn't find " + name + "[.sh|.bat], candidates: " + Arrays.toString(candidates));
        }

        File executable = candidates[0];
        System.out.println(name + " location: " + executable.getAbsolutePath());

        return executable;
    }

    public static File getBuildScript(String task, String defaultTemplate, WebSphereModel model, String workingDir, String ext) throws IOException {
        String template = getScriptTemplate(defaultTemplate, model, workingDir);
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(template);

        StringBuilder buildFile = new StringBuilder(50);
        buildFile.append(task);
        if (StringUtils.isNotBlank(model.getHost())) {
            buildFile.append("-").append(model.getHost());
        }
        if (StringUtils.isNotBlank(model.getApplicationName())) {
            buildFile.append("-").append(model.getApplicationName());
        }
        buildFile.append("-").append(getTimestampString()).append(".").append(ext);

        File buildScriptFile = new File(workingDir, buildFile.toString());
        buildScriptFile.getParentFile().mkdirs();
        Writer writer = new FileWriter(buildScriptFile);
        mustache.execute(writer, model.getProperties()).flush();

        return buildScriptFile;
    }

    private static String getScriptTemplate(String defaultTemplate, WebSphereModel model, String workingDir) {
        if (StringUtils.isEmpty(model.getScript())) {
            return defaultTemplate;
        } else {
            File customizedScript = null;
            if (model.getScript().startsWith(File.separator)) {
                customizedScript = new File(model.getScript());
            } else {
                File projectRoot = new File(workingDir).getParentFile().getParentFile().getParentFile();
                customizedScript = new File(projectRoot, model.getScript());
            }

            if (!customizedScript.exists()) {
                throw new WebSphereServiceException("Customized script doesn't exist: " + customizedScript.getAbsolutePath());
            }
            System.out.println("Using customized script: " + customizedScript.getAbsolutePath());
            return customizedScript.getAbsolutePath();
        }
    }

    public static void executeCommand(Commandline commandline, StreamConsumer outConsumer, StreamConsumer errorConsumer, boolean isVerbose)
            throws CommandLineException {
        if (isVerbose) {
            System.out.println("Executing command:\n" + StringUtils.join(commandline.getShellCommandline(), " "));
        }

        int returnCode = CommandLineUtils.executeCommandLine(commandline, outConsumer, errorConsumer, 1800);

        if (returnCode != 0) {
            String msg = "Failed to deploy, return code: " + returnCode + ". Please make sure target WAS is alive and reachable.";
            throw new WebSphereServiceException(msg);
        } else {
            System.out.println("Return code: " + returnCode);
        }
    }

    public static String getTimestampString() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
    }
}
