package com.orctom.mojo.was;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * Created by CH on 3/4/14.
 */
@Mojo(name = "install", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST, requiresDirectInvocation = true, threadSafe = true)
public class WASInstallMojo extends AbstractWASMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }
}
