// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
