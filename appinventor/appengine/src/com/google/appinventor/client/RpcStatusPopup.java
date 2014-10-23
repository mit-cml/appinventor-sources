// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client;

import static com.google.appinventor.client.Ode.MESSAGES;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.WindowResizeListener;
import com.google.gwt.user.client.WindowScrollListener;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;

import java.util.HashMap;
import java.util.Map;

/**
 * Popup that shows a status message while an asynchronous request is
 * happening.
 *
 */
public class RpcStatusPopup extends DecoratedPopupPanel implements RpcListener {
  // Map from method names to messages
  private static final Map<String, String> statusMessages = initMessages();

  // Label that shows the message for the current RPC
  private final Label label = new Label();

  // Number of active RPCs. The popup is shown iff this is positive.
  private int activeRPCs = 0;

  /**
   * Initializes the LoadingPopup.
   */
  public RpcStatusPopup() {
    super(/* autoHide = */ false);
    setStyleName("ode-RpcStatusMessage");
    setWidget(label);

    // Re-center the loading message when the window is resized.
    // TODO(halabelson): Replace the deprecated methods
    Window.addWindowResizeListener(new WindowResizeListener() {
      @Override
      public void onWindowResized(int width, int height) {
        positionPopup(getOffsetWidth());
      }
    });

    // Reposition the message to the top of the screen when the
    // window is scrolled
    // TODO(halabelson): get rid of the flashing on vertical scrolling
    Window.addWindowScrollListener(new WindowScrollListener() {
      @Override
      public void onWindowScrolled(int scrollLeft, int scrollTop) {
        positionPopup(getOffsetWidth());
      }
    });

    // Position the popup before showing it to prevent flashing.
    setPopupPositionAndShow(new PopupPanel.PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        positionPopup(offsetWidth);
      }
    });
  }

  /**
   * Register a service proxy with the loading popup. The loading popup will
   * listen to the RPCs on the service and show/hide itself automatically.
   *
   * @param service the service to monitor
   */
  public void register(ExtendedServiceProxy<?> service) {
    service.addRpcListener(this);
  }

  /**
   * Unregister a service proxy with the loading popup.
   *
   * @param service the service to monitor
   */
  public void unregister(ExtendedServiceProxy<?> service) {
    service.removeRpcListener(this);
  }

  // RpcListener implementation

  @Override
  public void onStart(String method, Object... params) {
    // TODO(user): This is very primitive in the case of multiple RPCs. The
    // message of the RPC started last succeeds. Its message will stay active
    // until all RPCs have finished or a new RPC starts.
    String message = statusMessages.get(method);
    label.setText(message != null ? message : MESSAGES.defaultRpcMessage());

    // Clear error display
    ErrorReporter.hide();

    countRPCs(+1);
  }

  @Override
  public void onFailure(String method, Throwable caught) {
    countRPCs(-1);
  }

  @Override
  public void onSuccess(String method, Object result) {
    countRPCs(-1);
  }

  private void countRPCs(int i) {
    activeRPCs += i;
    if (activeRPCs > 0) {
      show();
    } else {
      hide();
    }
  }

  private void positionPopup(int offsetWidth) {
    // make sure the popup is on-screen
    // if the top of the window is scrolled off the screen
    setPopupPosition(
        (Window.getClientWidth() - offsetWidth) >> 1,
        Window.getScrollTop());
  }

  private static Map<String, String> initMessages() {
    Map<String, String> msgs = new HashMap<String, String>();

    // RPC methods that show a "Saving..." message
    String saving = MESSAGES.savingRpcMessage();
    msgs.put("newProject", saving);
    msgs.put("save", saving);
    msgs.put("storeProjectSettings", saving);
    msgs.put("storeUserSettings", saving);

    // RPC methods that show a "Copying..." message
    msgs.put("copyProject", MESSAGES.copyingRpcMessage());

    // RPC methods that show a "Deleting..." message
    String deleting = MESSAGES.deletingRpcMessage();
    msgs.put("deleteFile", deleting);
    msgs.put("deleteFiles", deleting);
    msgs.put("deleteProject", deleting);

    // RPC methods that show a "Packaging..." message
    msgs.put("build", MESSAGES.packagingRpcMessage());

    // All other RPC methods will show the default "Loading..." message.

    return msgs;
  }
}
