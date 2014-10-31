// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;

/**
 *
 *
 * Helper to manage ClientLogin authentications tokens.
 * This interacts with the Google Account manager to add the
 * client login specific Authentication headers on each http request.
 * (Inspired by chenplim's "comm" package)
 *
 */

public class ClientLoginHelper implements IClientLoginHelper {
  private static final String LOG_TAG = "ClientLoginHelper";
  private static final String ACCOUNT_TYPE = "com.google";
  private static final String AUTHORIZATION_HEADER_PREFIX = "GoogleLogin auth=";

  private String service;
  private HttpClient client;
  private Activity activity;
  private AccountManager accountManager;
  private AccountChooser accountChooser;

  private String authToken;
  private boolean initialized = false;

  /**
   * Create one of these for each HttpClient needing clientlogin authentication.
   * @param activity        An activity that can be used for user interaction.
   * @param service         The application service class (e.g. "fusiontables").
   * @param prompt          The user prompt (if needed) to choose an account.
   * @param client          The HttpClient to use (or null for a default one).
   */
  public ClientLoginHelper(Activity activity, String service, String prompt, HttpClient client) {
    this.service = service;
    this.client = (client == null) ? new DefaultHttpClient() : client;
    this.activity = activity;
    this.accountManager = AccountManager.get(activity);
    this.accountChooser =  new AccountChooser(activity, service, prompt, service);
  }

  /**
   * Initialize the token, prompting the user for an account as needed.
   * This can block waiting for user action, so it can't be on the UI thread
   */
  private void initialize() throws ClientProtocolException {
    if (!initialized) {
      Log.i(LOG_TAG, "initializing");
      if (isUiThread()) {
        throw new IllegalArgumentException("Can't initialize login helper from UI thread");
      }
      authToken = getAuthToken();
      initialized = true;
    }
  }

  private boolean isUiThread() {
    return Looper.getMainLooper().getThread().equals(Thread.currentThread());
  }

  /**
   * Wraps an HttpClient.execute() to manage the authorization headers.
   * This will add the proper Authorization header, and retry if the
   * auth token has expired.
   */
  @Override
  public HttpResponse execute(HttpUriRequest request)
      throws ClientProtocolException, IOException {
    initialize();
    addGoogleAuthHeader(request, authToken);
    HttpResponse response = client.execute(request);
    if (response.getStatusLine().getStatusCode() == 401) {
      Log.i(LOG_TAG, "Invalid token: " + authToken);
      accountManager.invalidateAuthToken(ACCOUNT_TYPE, authToken);
      authToken = getAuthToken();
      removeGoogleAuthHeaders(request);
      addGoogleAuthHeader(request, authToken);
      Log.i(LOG_TAG, "new token: " + authToken);
      response = client.execute(request);
    }
    return response;
  }


  /**
   * Forget about the account the user chose.
   * The AccountChooser remembers (in shared prefs) the
   * chosen account.  Call this if you want to change the account
   * this service is associated with.
   */
  @Override
  public void forgetAccountName() {
    accountChooser.forgetAccountName();
  }

  private static void addGoogleAuthHeader(HttpUriRequest request, String token) {
    if (token != null) {
      Log.i(LOG_TAG, "adding auth token token: " + token);
      request.addHeader("Authorization", AUTHORIZATION_HEADER_PREFIX  + token);
    }
  }

  private static void removeGoogleAuthHeaders(HttpUriRequest request) {
    for (Header header : request.getAllHeaders()) {
      if (header.getName().equalsIgnoreCase("Authorization") &&
              header.getValue().startsWith(AUTHORIZATION_HEADER_PREFIX)) {
        Log.i(LOG_TAG, "Removing header:" + header);
        request.removeHeader(header);
      }
    }
  }

  /**
   * Uses Google Account Manager to retrieve auth token that can
   * be used to access various Google APIs -- e.g., the Google Voice api.
   */
  public String getAuthToken() throws ClientProtocolException {
    Account account = accountChooser.findAccount();
    if (account != null) {
      AccountManagerFuture<Bundle> future;
      future = accountManager.getAuthToken(account, service, null, activity, null, null);
      Log.i(LOG_TAG, "Have account, auth token: " + future);
      Bundle result;
      try {
        result = future.getResult();
        return result.getString(AccountManager.KEY_AUTHTOKEN);
      } catch (AuthenticatorException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      } catch (OperationCanceledException e) {
        e.printStackTrace();
      }
    }
    throw new ClientProtocolException("Can't get valid authentication token");
  }
}
