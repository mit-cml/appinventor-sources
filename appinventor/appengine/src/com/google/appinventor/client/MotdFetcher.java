// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.appinventor.client.output.MotdUi;
import com.google.appinventor.client.output.OdeLog;
import com.google.appinventor.shared.rpc.Motd;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Polls periodically for new MOTDs and displays them.
 *
 * @author kerr@google.com (Debby Wallach)
 */
public class MotdFetcher {
  private final int timerMillis; // how long to wait between fetches
  private final Timer timer;

  MotdFetcher(int timerSeconds) {
    timerMillis = timerSeconds * 1000;
    timer = new Timer() {
      public void run() {
        fetchMotd();
      }
    };
  }

  public void start() {
    fetchMotd();
    timer.scheduleRepeating(timerMillis);
  }

  private void fetchMotd() {
    AsyncCallback<Motd> callback = new AsyncCallback<Motd>() {
      @Override
      public void onFailure(Throwable caught) {
        OdeLog.log(MESSAGES.getMotdFailed());
      }

      @Override
      public void onSuccess(Motd motd) {
        MotdUi.getMotd().setMotd(motd);
      }
      // Should we display a default motd onFailure?
    };

    Ode.getInstance().getGetMotdService().getMotd(callback);
  }
}
