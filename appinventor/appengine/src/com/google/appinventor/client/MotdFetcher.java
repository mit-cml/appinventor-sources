// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.output.MotdUi;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.Motd;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Polls for new MOTDs and displays them.
 *
 * <p>We don't use a timer because it would keep the server busy with MOTD fetches even when the
 * user isn't actively using App Inventor. Instead, we fetch the MOTD when another RPC is going to
 * the server and the specified interval of time has elapsed since the last MOTD fetch. This way,
 * we only fetch the MOTD if the user is actively using App Inventor.</p>
 *
 * @author kerr@google.com (Debby Wallach)
 */
class MotdFetcher implements RpcListener {
  private final int intervalMillis; // how long to wait between fetches
  private long lastFetchTime;

  MotdFetcher(int intervalSecs) {
    intervalMillis = intervalSecs * 1000;
    fetchMotd();
  }

  /**
   * Register a service proxy with this MOTD fetcher.
   *
   * @param service the service to monitor
   */
  void register(ExtendedServiceProxy<?> service) {
    service.addRpcListener(this);
  }

  /**
   * Unregister a service proxy with the loading popup.
   *
   * @param service the service to monitor
   */
  void unregister(ExtendedServiceProxy<?> service) {
    service.removeRpcListener(this);
  }

  // RpcListener implementation

  @Override
  public void onStart(String method, Object... params) {
    // When an RPC is going to the server, we check whether enough time has elapsed that we should
    // fetch the MOTD.
    long now = System.currentTimeMillis();
    if (now - lastFetchTime >= intervalMillis) {
      fetchMotd();
    }
  }

  @Override
  public void onFailure(String method, Throwable caught) {
  }

  @Override
  public void onSuccess(String method, Object result) {
  }

  private void fetchMotd() {
    lastFetchTime = System.currentTimeMillis();

    AsyncCallback<Motd> callback = new AsyncCallback<Motd>() {
      @Override
      public void onFailure(Throwable caught) {
        OdeLog.log(MESSAGES.getMotdFailed());
      }

      @Override
      public void onSuccess(Motd motd) {
        MotdUi.getMotd().setMotd(motd);
      }
    };

    Ode.getInstance().getGetMotdService().getMotd(callback);
  }
}
