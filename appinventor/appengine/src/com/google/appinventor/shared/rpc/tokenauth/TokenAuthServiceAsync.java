package com.google.appinventor.shared.rpc.tokenauth;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Interface for the service providing user related information. All
 * declarations in this interface are mirrored in {@link TokenAuthService}.
 * For further information see {@link TokenAuthService}.
 *
 * @author joymitro1989@gmail.com (Joydeep Mitra)
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */
public interface TokenAuthServiceAsync {

    void getCloudDBToken(AsyncCallback<String> callback);
    void getTranslateToken(AsyncCallback<String> callback);
    void getChatBotToken(AsyncCallback<String> callback);
}
