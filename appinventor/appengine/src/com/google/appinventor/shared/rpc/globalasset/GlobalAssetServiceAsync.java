package com.google.appinventor.shared.rpc.globalasset;

import com.google.gwt.user.client.rpc.AsyncCallback;
import java.util.List;

public interface GlobalAssetServiceAsync {
  void getGlobalAssetPaths(AsyncCallback<List<String>> callback);
}
