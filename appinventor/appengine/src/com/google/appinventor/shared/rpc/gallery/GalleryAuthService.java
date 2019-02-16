package com.google.appinventor.shared.rpc.gallery;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 *  Service interface for the Gallery authentication RPC.
 */
@RemoteServiceRelativePath(ServerLayout.GALLERY_AUTH_SERVICE)
public interface GalleryAuthService extends RemoteService {

  String getToken();
}
