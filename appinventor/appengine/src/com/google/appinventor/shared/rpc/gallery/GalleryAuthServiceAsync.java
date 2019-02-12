package com.google.appinventor.shared.rpc.gallery;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Interface for the service providing user related information. All
 * declarations in this interface are mirrored in {@link GalleryAuthService}.
 * For further information see {@link GalleryAuthService}.
 */
public interface GalleryAuthServiceAsync {

  void getToken(AsyncCallback<String> callback);
}
