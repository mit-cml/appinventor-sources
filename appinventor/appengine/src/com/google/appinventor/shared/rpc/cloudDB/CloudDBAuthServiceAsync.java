package com.google.appinventor.shared.rpc.cloudDB;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Interface for the service providing user related information. All
 * declarations in this interface are mirrored in {@link CloudDBAuthService}.
 * For further information see {@link CloudDBAuthService}.
 *
 * @author joymitro1989@gmail.com (Joydeep Mitra)
 */
public interface CloudDBAuthServiceAsync {

    void getToken(AsyncCallback<String> callback);
}
