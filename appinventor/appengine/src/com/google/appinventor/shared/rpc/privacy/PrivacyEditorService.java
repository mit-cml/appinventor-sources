package com.google.appinventor.shared.rpc.privacy;

import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath(ServerLayout.PRIVACY_SERVICE)
public interface PrivacyEditorService extends RemoteService {
  String getPreview(long projectId);
}
