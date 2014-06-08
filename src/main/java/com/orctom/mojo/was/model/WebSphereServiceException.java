package com.orctom.mojo.was.model;

/**
 * Created by CH on 3/4/14.
 */
public class WebSphereServiceException extends RuntimeException {

    public WebSphereServiceException(String message) {
        super(message);
    }

    public WebSphereServiceException( Throwable cause) {
        super(cause);
    }

    public WebSphereServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
