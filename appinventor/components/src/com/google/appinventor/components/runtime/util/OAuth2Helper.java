package com.google.appinventor.components.runtime.util;

import java.io.IOException;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.SharedPreferences;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;


/**
 * This helper class uses AccountManager to handle OAuth 2.0 authorization,
 * returning an access_token that is needed for Fusiontables requests.  The
 * helper contains no reference to Fusiontables, so perhaps it can be 
 * generalized to handle OAuth for other Google APIs. 
 * 
 * @author rmorelli
 * 
 * This follows examples in the Google API Client libraries, especially the 
 * tasks-android-sample and the fusiontables-commandline-sample.
 * 
 * Here's an good overview:
 * https://developers.google.com/accounts/docs/OAuth2
 * 
 * Here's how it works.
 * 
 * Access by an app (a client) to an shared resource (a fusiontable) requires
 * the app to obtain an access token.  The access token must be attached in
 * the Http request header for every query to the shared resource. The access
 * token is usually valid for 1 hour and then it must be refreshed.
 * 
 * In order to obtain access to the resource the app must use an API key. This
 * is obtained by registering through your Google API console. 
 * 
 * Google AccountManager will manage the request for the access token, hiding 
 * most of the details. There are two cases:
 * 
 *   1) The app has stored the user's account name and the user is already
 *   logged in to a Google account.  In this case AccountManager uses the 
 *   account to obtain an access token.
 *   
 *   2) The app has not yet stored the user's account name.  In this case
 *   the AccountManager uses "features" to obtain the token, prompting the
 *   user to choose an account if there are more than one on the phone.
 *   
 * In either case OAuthHelper saves the user's account name and acquired
 * access token (called, traditionally, authToken) in shared preferences. 
 * 
 * Access/Refresh Token
 * 
 * If an expired access token is passed to a fusiontables query, the 
 * query will be rejected with a 401 error. In that case, the app must
 * get a fresh token.  This can be done by using a 'refresh token'.  
 * But it can also be done more simply by discarding the saved access 
 * token and acquiring a fresh one for each query.  That's the approach 
 * taken here.
 * 
 * Comments in the code provide the details.
 *   
 */
public class OAuth2Helper {  

  public static final String TAG = "OAuthHelper";
  public static final String PREF_AUTH_TOKEN      = "authToken";
  public static final String PREF_ACCOUNT_NAME = "accountName";
  
  private static String errorMessage = "Error during OAuth";

  public OAuth2Helper () {}

  /**
   * Uses account manager to return a fresh authToken of authTokenType
   * @param activity
   * @param authTokenType a service such as "fusiontables"
   * @return an access token string that must be added to the OAuth header in all 
   *   resource queries, or null if something goes wrong.
   */
  public String getRefreshedAuthToken(Activity activity, String authTokenType) {
    Log.i(TAG, "getRefreshedAuthToken()");

    if (isUiThread()) 
      throw new IllegalArgumentException("Can't get authtoken from UI thread");

    // Get the saved account name, possibly null
    SharedPreferences settings = activity.getPreferences(Activity.MODE_PRIVATE);
    String accountName = settings.getString(PREF_ACCOUNT_NAME, null);

    // Get the saved authToken, possibly null. We save even though we refresh it every time
    String authToken = settings.getString(PREF_AUTH_TOKEN, null);

    // Initialize credential with the saved authToken.
    GoogleCredential credential = new GoogleCredential();
    credential.setAccessToken(authToken);

    // Tell AccountManager to get the authToken and user account name. This is 
    //  where all the details of the OAuth flow are hidden.
    AccountManagerFuture<Bundle> future = 
      getAccountManagerResult(activity, credential, authTokenType, accountName); 

    // Extract and save the authToken and account name
    try {

      Bundle authTokenBundle = future.getResult();
      authToken = authTokenBundle.get(AccountManager.KEY_AUTHTOKEN).toString();

      persistCredentials(settings, 
          authTokenBundle.getString(AccountManager.KEY_ACCOUNT_NAME), 
          authToken);
    } catch (OperationCanceledException e) {
      e.printStackTrace();
      OAuth2Helper.resetAccountCredential(activity);
      errorMessage = "Error: operation cancelled";
    } catch (AuthenticatorException e) {
      e.printStackTrace();
      errorMessage = "Error: Authenticator error";
    } catch (IOException e) {
      e.printStackTrace();
      errorMessage = "Error: I/O error";
    }    

    // Return the authToken or null
    return authToken;
  }

  /**
   * An AccountManagerFuture represents the result of an asynchronous AccountManager call. 
   * The result can only be retrieved using method get when the call has completed, 
   * blocking if necessary until it is ready.
   * 
   * @param activity
   * @param credential
   * @param authTokenType
   * @return a Bundle containing the result of the call, possibly null
   */
  private AccountManagerFuture<Bundle> getAccountManagerResult(Activity activity, 
      GoogleCredential credential, 
      String authTokenType, 
      String accountName) {

    AccountManagerFuture<Bundle> future = null; 
    GoogleAccountManager accountManager = new GoogleAccountManager(activity);

    // Force the return of fresh token by invalidating the current token
    // Doing this on every OAuth request, avoids the need to determine whether the
    // authToken has expired, usually after 1 hour, and then getting 
    // another one using the refresh token.

    accountManager.invalidateAuthToken(credential.getAccessToken());
    AccountManager.get(activity).invalidateAuthToken(authTokenType, null);

    // Try to get the user's account by account name. Might return null

    Account account = accountManager.getAccountByName(accountName);

    // Here is where AccountManager may prompt user to select an account
    if (account != null) {

      // We have the user's account at this point, so AccountManager simply returns the token
      Log.i(TAG, "Getting token by account");
      future = accountManager.getAccountManager().getAuthToken(account, authTokenType, true, null, null);

    } else {

      // AccountManager uses 'features' to get the authToken, possibly prompting the user to choose an account
      Log.i(TAG, "Getting token by features, possibly prompting user to select an account");
      future = accountManager.getAccountManager().getAuthTokenByFeatures(GoogleAccountManager.ACCOUNT_TYPE,
          authTokenType, null, activity, null, null, null, null);      
    }

    // Return the whole bundle containing the authToken, account name, and other data.
    return future;
  }

  /**
   * Returns true if run on the UI thread. OAuth requests should not be made on the UI thread.
   */
  private boolean isUiThread() {
    return Looper.getMainLooper().getThread().equals(Thread.currentThread());
  }


  /**
   * Remember the account name and authToken in prefs.
   * @param accountName
   * @param authToken
   */
  private void persistCredentials(SharedPreferences settings, String accountName, String authToken) {
    Log.i(TAG, "Persisting credentials, acct =" + accountName);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(PREF_ACCOUNT_NAME, accountName);
    editor.putString(PREF_AUTH_TOKEN, authToken);
    editor.commit();
  }

  /**
   * Forget the account name and authToken.  With no account name the app will prompt 
   * the user to select a new account. This method is mostly used for testing purposes. 
   * 
   * @param activity
   */
  public static void resetAccountCredential(Activity activity) {
    Log.i(TAG, "Reset credentials");
    SharedPreferences settings = activity.getPreferences(Activity.MODE_PRIVATE);
    SharedPreferences.Editor editor2 = settings.edit();
    editor2.remove(PREF_AUTH_TOKEN);
    editor2.remove(PREF_ACCOUNT_NAME);
    editor2.commit();
  }
  
  /**
   * Clients can retrieve error messages statically.
   * @return errorMessage
   */
  public static String getErrorMessage() {
    Log.i(TAG, "getErrorMessage = " + errorMessage);
    return errorMessage;
  }
}
