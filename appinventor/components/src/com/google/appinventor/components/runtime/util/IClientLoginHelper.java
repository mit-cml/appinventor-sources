// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.components.runtime.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;

/**
 * An interface defining the functionality of ClientLoginHelper.
 * ClientLoginHelper can only be loaded by an Eclair (or later) Dalvik VM, but
 * this interface can be loaded by Cupcake and Donut.
 *
 * @author lizlooney@google.com (Liz Looney)
 */

public interface IClientLoginHelper {
  /**
   * Wraps an HttpClient.execute() to manage the authorization headers.
   * This will add the proper Authorization header, and retry if the
   * auth token has expired.
   */
  public HttpResponse execute(HttpUriRequest request) throws ClientProtocolException, IOException;

  /**
   * Forget about the account the user chose.
   * The AccountChooser remembers (in shared prefs) the
   * chosen account.  Call this if you want to change the account
   * this service is associated with.
   */
  public void forgetAccountName();
}
