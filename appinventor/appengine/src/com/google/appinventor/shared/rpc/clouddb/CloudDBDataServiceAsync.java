// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2026 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.clouddb;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * Async version of {@link CloudDBDataService} for use in GWT client code.
 * GWT generates the proxy implementation automatically via {@code GWT.create(CloudDBDataService.class)}.
 */
public interface CloudDBDataServiceAsync {

  void getEntries(String projectId, String token, String redisServer, int redisPort, boolean useSSL,
      AsyncCallback<List<DataEntry>> callback);

  void setEntry(String projectId, String token, String redisServer, int redisPort, boolean useSSL,
      String tag, String value, AsyncCallback<Void> callback);

  void deleteEntry(String projectId, String token, String redisServer, int redisPort, boolean useSSL,
      String tag, AsyncCallback<Void> callback);
}
