package com.orctom.mojo.was;

import com.google.common.base.Strings;
import com.orctom.was.model.Command;
import com.orctom.was.model.WebSphereModel;
import com.orctom.was.model.WebSphereServiceException;
import com.orctom.was.service.impl.AbstractWebSphereServiceImpl;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.*;

import java.io.File;

/**
 * Service to execute WAS tasks
 * Created by hao on 11/11/15.
 */
public class WebSphereServiceImpl extends AbstractWebSphereServiceImpl {

	public WebSphereServiceImpl(WebSphereModel model, String targetDir) {
		super(model, targetDir);
	}

	@Override
	protected void executeCommand(Command command) {
		try {
			Commandline commandLine = new Commandline();
			commandLine.setExecutable(command.getExecutable());
			commandLine.setWorkingDirectory(command.getWorkingDir());
			for (String arg : command.getArgs().keySet()) {
			    commandLine.createArg().setLine(arg);
			    if (!Strings.isNullOrEmpty(command.getArgs().get( arg ))) {
			        commandLine.createArg().setLine(StringUtils.quoteAndEscape( command.getArgs().get( arg ),'"' ) );
			    }
			    
			}

			StringStreamConsumer outConsumer = new StringStreamConsumer();
			StringStreamConsumer errConsumer = new StringStreamConsumer();
			executeCommand(commandLine, outConsumer, errConsumer, model.isVerbose());

			String out = outConsumer.getOutput();
			FileUtils.fileWrite(new File(command.getBuildScriptPath() + ".log"), out);

			if (model.isFailOnError() && (
					out.contains("com.ibm.ws.scripting.ScriptingException") || out.contains("com.ibm.bsf.BSFException"))) {
				throw new WebSphereServiceException("Failed to execute the task, Please see the log for more details");
			}

			String error = errConsumer.getOutput();
			if (StringUtils.isNotEmpty(error)) {
				System.err.println(error);
			}
		} catch (CommandLineTimeOutException e) {
			throw new WebSphereServiceException("Failed to execute the task." +
					"Please ensure remote WAS or Deployment Manager is running. " + e.getMessage(), e);
		} catch (Exception e) {
			throw new WebSphereServiceException(e.getMessage(), e);
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
