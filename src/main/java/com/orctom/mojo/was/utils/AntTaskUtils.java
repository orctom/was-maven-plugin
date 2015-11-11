package com.orctom.mojo.was.utils;

import com.orctom.mojo.was.Constants;
import com.orctom.was.model.WebSphereModel;
import com.orctom.was.utils.CommandUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DefaultLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.taskdefs.Typedef;
import org.apache.tools.ant.types.Path;
import org.codehaus.plexus.configuration.PlexusConfiguration;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * Utils to execute ant tasks
 * Created by CH on 3/19/14.
 */
public class AntTaskUtils {

    /**
     * The refid used to store the Maven project object in the Ant build.
     */
    private final static String DEFAULT_MAVEN_PROJECT_REFID = "maven.project";

    /**
     * The refid used to store the Maven project object in the Ant build.
     */
    private final static String DEFAULT_MAVEN_PROJECT_HELPER_REFID = "maven.project.helper";

    /**
     * The path to The XML file containing the definition of the Maven tasks.
     */
    private final static String ANTLIB = "org/apache/maven/ant/tasks/antlib.xml";

    /**
     * The default target name.
     */
    private final static String DEFAULT_ANT_TARGET_NAME = "main";

    public static void execute(WebSphereModel model, PlexusConfiguration target, MavenProject project,
                               MavenProjectHelper projectHelper, List<Artifact> pluginArtifact, Log logger)
            throws IOException, MojoExecutionException {
        // The fileName should probably use the plugin executionId instead of the targetName
        boolean useDefaultTargetName = false;
        String antTargetName = target.getAttribute("name");
        if (null == antTargetName) {
            antTargetName = DEFAULT_ANT_TARGET_NAME;
            useDefaultTargetName = true;
        }
        StringBuilder fileName = new StringBuilder(50);
        fileName.append("build");
        if (StringUtils.isNotBlank(model.getHost())) {
            fileName.append("-").append(model.getHost());
        }
        if (StringUtils.isNotBlank(model.getApplicationName())) {
            fileName.append("-").append(model.getApplicationName());
        }
        fileName.append("-").append(antTargetName).append("-").append(CommandUtils.getTimestampString()).append(".xml");
        File buildFile = getBuildFile(project, fileName.toString());

        if (model.isVerbose()) {
            logger.info("ant fileName: " + fileName);
        }

        if (buildFile.exists()) {
            logger.info("[SKIPPED] already executed");
            return;
        }

        StringWriter writer = new StringWriter();
        AntXmlPlexusConfigurationWriter xmlWriter = new AntXmlPlexusConfigurationWriter();
        xmlWriter.write(target, writer);

        StringBuffer antXML = writer.getBuffer();

        if (useDefaultTargetName) {
            stringReplace(antXML, "<target", "<target name=\"" + antTargetName + "\"");
        }

        final String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
        antXML.insert(0, xmlHeader);
        final String projectOpen = "<project name=\"" + Constants.PLUGIN_ID + "\" default=\"" + antTargetName + "\">\n";
        int index = antXML.indexOf("<target");
        antXML.insert(index, projectOpen);

        final String projectClose = "\n</project>";
        antXML.append(projectClose);

        buildFile.getParentFile().mkdirs();
        FileUtils.fileWrite(buildFile.getAbsolutePath(), "UTF-8", antXML.toString());

        Project antProject = generateAntProject(model, buildFile, project, projectHelper, pluginArtifact, logger);
        antProject.executeTarget(antTargetName);
    }

    private static File getBuildFile(MavenProject project, String fileName) {
        String path = project.getBuild().getDirectory() + File.separator + Constants.PLUGIN_ID + File.separator + "steps" + File.separator;
        return new File(path, fileName);
    }

    private static Project generateAntProject(WebSphereModel model, File antBuildFile, MavenProject project, MavenProjectHelper projectHelper,
                                              List<Artifact> pluginArtifact, Log logger)
            throws MojoExecutionException {
        try {
            Project antProject = new Project();
            ProjectHelper.configureProject(antProject, antBuildFile);
            antProject.init();

            setupLogger(antBuildFile, logger, antProject);

            antProject.setBaseDir(project.getBasedir());

            Path p = new Path(antProject);
            p.setPath(StringUtils.join(project.getCompileClasspathElements().iterator(), File.pathSeparator));

            antProject.addReference("maven.compile.classpath", p);

            p = new Path(antProject);
            p.setPath(StringUtils.join(project.getRuntimeClasspathElements().iterator(), File.pathSeparator));
            antProject.addReference("maven.runtime.classpath", p);

            p = new Path(antProject);
            p.setPath(StringUtils.join(project.getTestClasspathElements().iterator(), File.pathSeparator));
            antProject.addReference("maven.test.classpath", p);

            antProject.addReference("maven.plugin.classpath", getPathFromArtifacts(pluginArtifact, antProject));

            antProject.addReference(DEFAULT_MAVEN_PROJECT_REFID, project);
            antProject.addReference(DEFAULT_MAVEN_PROJECT_HELPER_REFID, projectHelper);
            initMavenTasks(antProject);

            copyProperties(project, antProject);
            copyProperties(model.getProperties(), antProject);

            return antProject;

        } catch (DependencyResolutionRequiredException e) {
            throw new MojoExecutionException("DependencyResolutionRequiredException: " + e.getMessage(), e);
        } catch (BuildException e) {
            throw new MojoExecutionException("An Ant BuildException has occurred: " + e.getMessage(), e);
        } catch (Throwable e) {
            throw new MojoExecutionException("Error executing ant tasks: " + e.getMessage(), e);
        }
    }

    private static void setupLogger(File antBuildFile, Log logger, Project antProject) throws FileNotFoundException {
        DefaultLogger consoleLogger = new DefaultLogger();
        consoleLogger.setOutputPrintStream(System.out);
        consoleLogger.setErrorPrintStream(System.err);

        addBuildListener(logger, antProject, consoleLogger);

        DefaultLogger fileLogger = new DefaultLogger();
        PrintStream ps = new PrintStream(new FileOutputStream(new File(antBuildFile.getAbsolutePath() + ".log")));
        fileLogger.setOutputPrintStream(ps);
        fileLogger.setErrorPrintStream(ps);

        addBuildListener(logger, antProject, fileLogger);
    }

    private static void addBuildListener(Log logger, Project antProject, DefaultLogger listener) {
        if (logger.isDebugEnabled()) {
            listener.setMessageOutputLevel(Project.MSG_DEBUG);
        } else if (logger.isInfoEnabled()) {
            listener.setMessageOutputLevel(Project.MSG_INFO);
        } else if (logger.isWarnEnabled()) {
            listener.setMessageOutputLevel(Project.MSG_WARN);
        } else if (logger.isErrorEnabled()) {
            listener.setMessageOutputLevel(Project.MSG_ERR);
        } else {
            listener.setMessageOutputLevel(Project.MSG_VERBOSE);
        }
        antProject.addBuildListener(listener);
    }

    private static void stringReplace(StringBuffer text, String match, String with) {
        int index = text.indexOf(match);
        if (index != -1) {
            text.replace(index, index + match.length(), with);
        }
    }

    private static Path getPathFromArtifacts(Collection<Artifact> artifacts, Project antProject)
            throws DependencyResolutionRequiredException {
        if (artifacts == null) {
            return new Path(antProject);
        }

        List<String> list = new ArrayList<String>(artifacts.size());
        for (Artifact a : artifacts) {
            File file = a.getFile();
            if (file == null) {
                throw new DependencyResolutionRequiredException(a);
            }
            list.add(file.getPath());
        }

        Path p = new Path(antProject);
        p.setPath(StringUtils.join(list.iterator(), File.pathSeparator));

        return p;
    }

    private static void initMavenTasks(Project antProject) {
        Typedef typedef = new Typedef();
        typedef.setProject(antProject);
        typedef.setResource(ANTLIB);
        typedef.execute();
    }

    private static void copyProperties(MavenProject mavenProject, Project antProject) {
        copyProperties(mavenProject.getProperties(), antProject);

        // Set the POM file as the ant.file for the tasks run directly in Maven.
        antProject.setProperty("ant.file", mavenProject.getFile().getAbsolutePath());

        // Add some of the common maven properties
        antProject.setProperty(("project.groupId"), mavenProject.getGroupId());
        antProject.setProperty(("project.artifactId"), mavenProject.getArtifactId());
        antProject.setProperty(("project.name"), mavenProject.getName());
        if (mavenProject.getDescription() != null) {
            antProject.setProperty(("project.description"), mavenProject.getDescription());
        }
        antProject.setProperty(("project.version"), mavenProject.getVersion());
        antProject.setProperty(("project.packaging"), mavenProject.getPackaging());
        antProject.setProperty(("project.build.directory"), mavenProject.getBuild().getDirectory());
        antProject.setProperty(("project.build.outputDirectory"), mavenProject.getBuild().getOutputDirectory());
        antProject.setProperty(("project.build.testOutputDirectory"), mavenProject.getBuild().getTestOutputDirectory());
        antProject.setProperty(("project.build.sourceDirectory"), mavenProject.getBuild().getSourceDirectory());
        antProject.setProperty(("project.build.testSourceDirectory"), mavenProject.getBuild().getTestSourceDirectory());
    }

    private static void copyProperties(Properties properties, Project antProject) {
        for (Object o : properties.keySet()) {
            String key = (String) o;
            antProject.setProperty(key, properties.getProperty(key));
        }
    }
}
