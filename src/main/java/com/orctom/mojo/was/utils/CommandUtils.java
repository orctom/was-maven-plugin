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
 * Created by CH on 3/13/14.
 */
public class CommandUtils {

    private static final String TIMESTAMP_FORMAT = "yyyyMMdd-HHmmss-SSS";

    public static String getWorkingDir(String targetFolder, String templateExt) {
        return targetFolder + "/" + Constants.PLUGIN_ID + "/" + templateExt + "/";
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

        if (candidates.length != 1) {
            throw new WebSphereServiceException("Couldn't find " + name + "[.sh|.bat], candidates: " + Arrays.toString(candidates));
        }

        File executable = candidates[0];
        System.out.println(name + " location: " + executable.getAbsolutePath());

        return executable;
    }

    public static File getBuildScript(String task, String template, WebSphereModel model, String workingDir, String ext)
            throws IOException {
        if (StringUtils.isNotEmpty(model.getScript())) {
            if (model.getScript().startsWith("/")) {
                return new File(model.getScript());
            } else {
                return new File(workingDir, model.getScript());
            }
        } else {
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
            mustache.execute(writer, model).flush();

            return buildScriptFile;
        }
    }

    public static void executeCommand(Commandline commandline, StreamConsumer outConsumer, StreamConsumer errorConsumer,
                                      boolean isVerbose) {
        try {
            if (isVerbose) {
                System.out.println("Executing command:\n" + StringUtils.join(commandline.getShellCommandline(), " "));
            }

            int returnCode = CommandLineUtils.executeCommandLine(commandline, outConsumer, errorConsumer, 1200);

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

    public static String getTimestampString() {
        return new SimpleDateFormat(TIMESTAMP_FORMAT).format(new Date());
    }
}
