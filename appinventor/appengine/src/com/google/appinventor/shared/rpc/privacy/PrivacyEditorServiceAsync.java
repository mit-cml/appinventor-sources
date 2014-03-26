package com.google.appinventor.shared.rpc.privacy;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;

public interface PrivacyEditorServiceAsync {
  void getPreview(long projectId, AsyncCallback<String> callback);
}
