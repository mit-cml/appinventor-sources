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
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.SynchronousQueue;

/**
 * Get an account to Authenticate an application to:
 * <ul>
 * <li> If there is only one account, use it
 * <li> If there are no accounts, create one and use it
 * <li> If there are multiple accounts:
 *   <ul>
 *     <li> prompt the user to choose an account
 *     <li> remember the choice for next time
 *   </ul>
 * </ul>
 */

public class AccountChooser {
  private static final String NO_ACCOUNT = "";
  private static final String LOG_TAG = "AccountChooser";
  private static final String ACCOUNT_PREFERENCE = "account";
  /**
   * Use the Google Client-login protocol
   */
  private static final String ACCOUNT_TYPE = "com.google";

  private AccountManager accountManager;
  private String service;
  private String preferencesKey;
  private Activity activity;
  private String chooseAccountPrompt;

  public AccountChooser(Activity activity, String service, String title, String key) {
    this.activity = activity;
    this.service = service;
    this.chooseAccountPrompt = title;
    this.preferencesKey = key;
    this.accountManager = AccountManager.get(activity);
  }

  /**
   * Find the account to use for this service
   */
  public Account findAccount() {
    Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);

    // only one matching account - use it, and remember it for the next time.
    if (accounts.length == 1) {
      persistAccountName(accounts[0].name);
      return accounts[0];
    }

    // No accounts, create one and use it if created, otherwise we have no account to use

    if (accounts.length == 0) {
      String accountName = createAccount();
      if (accountName != null) {
        persistAccountName(accountName);
        return accountManager.getAccountsByType(ACCOUNT_TYPE)[0];
      } else {
        Log.i(LOG_TAG, "User failed to create a valid account");
        return null;
      }
    }

    // Still valid previously chosen account - use it

    Account account;
    String accountName = getPersistedAccountName();
    if (accountName != null && (account = chooseAccount(accountName, accounts)) != null) {
       return account;
    }

    // Either there is no saved account name, or our saved account vanished
    // Have the user select the account

    accountName = selectAccount(accounts);
    if (accountName != null) {
      persistAccountName(accountName);
      return chooseAccount(accountName, accounts);
    }

    // user didn't choose an account at all!
    Log.i(LOG_TAG, "User failed to choose an account");
    return null;
  }

  private Account chooseAccount(String accountName, Account[] accounts) {
    for (Account account : accounts) {
      if (account.name.equals(accountName)) {
        Log.i(LOG_TAG, "chose account: " + accountName);
        return account;
      }
    }
    return null;
  }

  private String createAccount() {
    AccountManagerFuture<Bundle> future;
    Log.i(LOG_TAG, "Adding auth token account ...");
    future = accountManager.addAccount(ACCOUNT_TYPE, service, null, null, activity, null, null);
    try {
      Bundle result = future.getResult();
      String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
      Log.i(LOG_TAG, "created: " + accountName);
      return accountName;
    } catch (OperationCanceledException e) {
      e.printStackTrace();
    } catch (AuthenticatorException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  private String selectAccount(Account accounts[]) {
    final SynchronousQueue<String> queue = new SynchronousQueue<String>();
    SelectAccount select = new SelectAccount(accounts, queue);
    select.start();
    Log.i(LOG_TAG, "Select: waiting for user...");
    String account = null;
    try {
      account = queue.take();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    Log.i(LOG_TAG, "Selected: " + account);
    return account == NO_ACCOUNT ? null : account;
  }

  private SharedPreferences getPreferences() {
    return activity.getSharedPreferences(preferencesKey, Context.MODE_PRIVATE);
  }

  private String getPersistedAccountName() {
    return getPreferences().getString(ACCOUNT_PREFERENCE, null);
  }

  private void persistAccountName(String accountName) {
    Log.i(LOG_TAG, "persisting account: "  + accountName);
    getPreferences().edit().putString(ACCOUNT_PREFERENCE, accountName).commit();
  }

  public void forgetAccountName() {
    getPreferences().edit().remove(ACCOUNT_PREFERENCE).commit();
  }

  /**
   * Start a background thread that pops up a user dialog, then
   * blocks until the user has selected an account.
   *
   */
  class SelectAccount extends Thread implements OnClickListener, OnCancelListener {
    private String[] accountNames;
    private SynchronousQueue<String> queue;

    SelectAccount(Account[] accounts, SynchronousQueue<String> queue) {
      this.queue = queue;
      accountNames = new String[accounts.length];
      for (int i = 0; i < accounts.length; i++) {
        accountNames[i] = accounts[i].name;
      }
    }

    @Override
    public void run() {
      activity.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          AlertDialog.Builder ab = new AlertDialog.Builder(activity)
            .setTitle(Html.fromHtml(chooseAccountPrompt))
            .setOnCancelListener(SelectAccount.this)
            .setSingleChoiceItems(accountNames, -1, SelectAccount.this);
          ab.show();
          Log.i(LOG_TAG, "Dialog showing!");
        }
      });
    }

    @Override
    public void onClick(DialogInterface dialog, int button) {
      try {
        if (button >= 0) {
          String account = accountNames[button];
          Log.i(LOG_TAG, "Chose: " + account);
          queue.put(account);
        } else {
          queue.put(NO_ACCOUNT);
        }
      } catch (InterruptedException e) {
        // This should never happen
      }
      dialog.dismiss();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
      Log.i(LOG_TAG, "Chose: canceled");
      onClick(dialog, -1);
    }
  }
}
