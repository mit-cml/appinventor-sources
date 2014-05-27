package com.google.appinventor.shared.rpc.privacy;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

public interface PrivacyEditorServiceAsync {
  void getPrivacyTTL(long projectId, AsyncCallback<String> callback);
  void getPrivacyHTML(long projectId, AsyncCallback<String> callback);
}
