package com.orctom.mojo.was.service;

import com.orctom.mojo.was.model.WebSphereModel;
import com.orctom.mojo.was.service.impl.WebSphereServiceAntImpl;
import com.orctom.mojo.was.service.impl.WebSphereServiceScriptImpl;

/**
 * Created by CH on 3/17/14.
 */
public class WebSphereServiceFactory {

    private WebSphereServiceFactory() {
    }

    public enum Service {
        ANT, SCRIPT;
    }

    public static IWebSphereService getService(String mode, WebSphereModel model, String workingDir) {
        Service service = Service.valueOf(mode.toUpperCase());
        switch (service) {
            case SCRIPT:
                return new WebSphereServiceScriptImpl(model, workingDir);
            case ANT:
                return new WebSphereServiceAntImpl(model, workingDir);
            default:
                return new WebSphereServiceScriptImpl(model, workingDir);
        }
    }
}
