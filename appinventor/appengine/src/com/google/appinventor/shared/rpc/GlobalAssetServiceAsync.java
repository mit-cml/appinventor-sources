// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc;

import com.google.appinventor.shared.rpc.project.GlobalAsset;
import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * Asynchronous counterpart of {@link GlobalAssetService}.
 *
 */
public interface GlobalAssetServiceAsync {
  /**
   * See {@link GlobalAssetService#getGlobalAssets()}
   */
  void getGlobalAssets(AsyncCallback<List<GlobalAsset>> callback);

  /**
   * See {@link GlobalAssetService#deleteGlobalAsset(String)}
   */
  void deleteGlobalAsset(String fileName, AsyncCallback<Void> callback);

  /**
   * See {@link GlobalAssetService#linkGlobalAssetToProject(long, String, long)}
   */
  void linkGlobalAssetToProject(long projectId, String globalAssetId, long timestamp, AsyncCallback<Void> callback);

  /**
   * See {@link GlobalAssetService#isGlobalAssetUpdated(String, long)}
   */
  void isGlobalAssetUpdated(String globalAssetId, long currentTimestamp, AsyncCallback<Boolean> callback);

  /**
   * See {@link GlobalAssetService#getGlobalAsset(String)}
   */
  void getGlobalAsset(String fileName, AsyncCallback<GlobalAsset> callback);
}
