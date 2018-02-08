package com.google.appinventor.shared.rpc.cloudDB;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 *  Service interface for the CloudDB authentication RPC.
 * @author joymitro1989@gmail.com (Joydeep Mitra).
 */
@RemoteServiceRelativePath(ServerLayout.CLOUD_DB_AUTH_SERVICE)
public interface CloudDBAuthService extends RemoteService{

    String getToken();
}
