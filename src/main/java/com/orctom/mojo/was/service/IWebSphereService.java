package com.orctom.mojo.was.service;

import java.util.Collection;

/**
 * Created by CH on 3/11/14.
 */
public interface IWebSphereService {

    void restartServer();

    Collection<String> listApplications();

    void installApplication();

    void uninstallApplication();

    void startApplication();

    void stopApplication();

    boolean isApplicationInstalled();
}
