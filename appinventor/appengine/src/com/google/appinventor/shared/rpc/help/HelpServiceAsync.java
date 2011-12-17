// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.shared.rpc.help;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Interface for the service providing help information. All
 * declarations in this interface are mirrored in {@link HelpService}.
 * For further information see {@link HelpService}.
 *
 */
public interface HelpServiceAsync {

  /**
   * @see HelpService#isProductionServer()
   */
  void isProductionServer(AsyncCallback<Boolean> callback);
}
