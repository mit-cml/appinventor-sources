// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.local;

import com.google.appinventor.shared.rpc.GetMotdServiceAsync;
import com.google.appinventor.shared.rpc.Motd;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class LocalGetMotdService implements GetMotdServiceAsync {
  @Override
  public void getMotd(AsyncCallback<Motd> callback) {
    Motd motd = new Motd(0, "", "");
    callback.onSuccess(motd);
  }

  @Override
  public void getCheckInterval(AsyncCallback<Integer> callback) {
    callback.onSuccess(0);
  }
}
