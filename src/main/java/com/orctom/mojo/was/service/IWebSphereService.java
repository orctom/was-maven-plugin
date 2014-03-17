package com.orctom.mojo.was.service;

/**
 * Created by CH on 3/11/14.
 */
public interface IWebSphereService {

    void restartServer();

    void installApplication();

    void uninstallApplication();

    void startApplication();

    void stopApplication();

    /**
     * uninstall-if-exists - > install -> restart
     */
    void deploy();
}
