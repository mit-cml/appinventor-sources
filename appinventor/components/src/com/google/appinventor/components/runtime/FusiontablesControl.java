// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.appinventor.components.runtime;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.googleapis.services.GoogleKeyInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.fusiontables.Fusiontables;
import com.google.api.services.fusiontables.Fusiontables.Query.Sql;
import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ClientLoginHelper;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.IClientLoginHelper;
import com.google.appinventor.components.runtime.util.OAuth2Helper;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Appinventor fusiontables control.
 *
 * This version has been migrated from the Fusiontables SQL API to the Fusiontables V1.0 API.
 *
 * See <a href="https://developers.google.com/fusiontables/">https://developers.google.com/fusiontables/</a>
 * See <a href="https://developers.google.com/fusiontables/docs/v1/migration_guide">https://developers.google.com/fusiontables/</a>
 *
 * The main change occurs in the way API requests are authorized.  This version uses
 * OAuth 2.0 and makes use of OAuth2Helper. The helper uses the Google AccountManager
 * to acquire an access token that must be attached as the OAuth header in all
 * Fusiontable Http requests.
 *
 * Before a Fusiontable request can be made, the app must acquire an OAuth token.
 * This may involve the user logging in to their Gmail account (or not if they are already
 * logged in) and then being prompted to give the app permission to access the user's fusion
 * tables.
 *
 * Permission takes the form of an access token (called authToken), which must be
 * transmitted to the Fusiontables service as part of all Http requests.
 *
 */
@DesignerComponent(version = YaVersion.FUSIONTABLESCONTROL_COMPONENT_VERSION,
    description = "<p>A non-visible component that communicates with Google Fusion Tables. " +
    "Fusion Tables let you store, share, query and visualize data tables; " +
    "this component lets you query, create, and modify these tables.</p> "  +
    "<p>This component uses the " +
    "<a href=\"https://developers.google.com/fusiontables/docs/v1/getting_started\" target=\"_blank\">Fusion Tables API V1.0</a>. " +
    "<p>In order to develop apps that use Fusiontables, you must obtain an API Key." +
    "<p>To get an API key, follow these instructions.</p> " +
    "<ol>" +
    "<li>Go to your <a href=\"https://code.google.com/apis/console/\" target=\"_blank\">Google APIs Console</a> and login if necessary.</li>" +
    "<li>Select the <i>Services</i> item from the menu on the left.</li>"   +
    "<li>Choose the <i>Fusiontables</i> service from the list provided and turn it on.</li>" +
    "<li>Go back to the main menu and select the <i>API Access</i> item. </li>" +
    "</ol>" +
    "<p>Your API Key will be near the bottom of that pane in the section called \"Simple API Access\"." +
    "You will have to provide that key as the value for the <i>ApiKey</i> property in your Fusiontables app.</p>" +
    "<p>Once you have an API key, set the value of the <i>Query</i> property to a valid Fusiontables SQL query " +
    "and call <i>SendQuery</i> to execute the query.  App Inventor will send the query to the Fusion Tables " +
    "server and the <i>GotResult</i> block will fire when a result is returned from the server." +
    "Query results will be returned in CSV format, and " +
    "can be converted to list format using the \"list from csv table\" or " +
    "\"list from csv row\" blocks.</p>" +
    "<p>Note that you do not need to worry about UTF-encoding the query. " +
    "But you do need to make sure the query follows the syntax described in " +
    "<a href=\"https://developers.google.com/fusiontables/docs/v1/getting_started\" target=\"_blank\">the reference manual</a>, " +
    "which means that things like capitalization for names of columns matters, and " +
    "that single quotes must be used around column names if there are spaces in them.</p>",
    category = ComponentCategory.EXPERIMENTAL,
    nonVisible = true,
    iconName = "images/fusiontables.png")
@SimpleObject
@UsesPermissions(permissionNames =
    "android.permission.INTERNET," +
    "android.permission.ACCOUNT_MANAGER," +
    "android.permission.MANAGE_ACCOUNTS," +
    "android.permission.GET_ACCOUNTS," +
    "android.permission.USE_CREDENTIALS")
@UsesLibraries(libraries =
    "fusiontables.jar," +
    "google-api-client-beta.jar," +
    "google-api-client-android2-beta.jar," +
    "google-http-client-beta.jar," +
    "google-http-client-android2-beta.jar," +
    "google-http-client-android3-beta.jar," +
    "google-oauth-client-beta.jar," +
    "guava-14.0.1.jar")

public class FusiontablesControl extends AndroidNonvisibleComponent implements Component {
  private static final String LOG_TAG = "fusion";
  private static final String DIALOG_TEXT = "Choose an account to access FusionTables";
  private static final String FUSION_QUERY_URL = "http://www.google.com/fusiontables/api/query";
  public static final String FUSIONTABLES_POST = "https://www.googleapis.com/fusiontables/v1/tables";

  private static final String DEFAULT_QUERY = "show tables";
  private static final String FUSIONTABLES_SERVICE = "fusiontables";
  private static final int SERVER_TIMEOUT_MS = 30000;
  public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";


  public static final String FUSIONTABLES_URL = "https://www.googleapis.com/fusiontables/v1/query";
  public static final String AUTH_TOKEN_TYPE_FUSIONTABLES = "oauth2:https://www.googleapis.com/auth/fusiontables";
  public static final String APP_NAME = "App Inventor";

  private String authTokenType = AUTH_TOKEN_TYPE_FUSIONTABLES;

  /**
   * The developer's Google API key,
   * See <a href="https://code.google.com/apis/console/">https://code.google.com/apis/console/</a>
   */
  private String apiKey;


  /**
   * The query to send to the Fusiontables service.
   */
  private String query;

  /**
   * String result of API query
   */
  private String queryResultStr;

  /**
   * Error message returned from API query
   */
  private String errorMessage = "Error on Fusiontables query";

  private final Activity activity;
  private final IClientLoginHelper requestHelper;

  public FusiontablesControl(ComponentContainer componentContainer) {
    super(componentContainer.$form());
    this.activity = componentContainer.$context();
    requestHelper = createClientLoginHelper(DIALOG_TEXT, FUSIONTABLES_SERVICE);
    query = DEFAULT_QUERY;

    if (SdkLevel.getLevel() < SdkLevel.LEVEL_ECLAIR) {
      showNoticeAndDie(
          "Sorry. The Fusiontables component is not compatible with this phone.",
          "This application must exit.",
          "Rats!");
    }

    // comment: The above code was originally
    //    Toast.makeText(activity,
    //        "Sorry. The Fusiontables component is not compatible with your phone. Exiting.",
    //        Toast.LENGTH_LONG).show();
    //    activity.finish();
    // I'm leaving this here for the edification of future developers.  The code does not work
    // because Toasts do not block: The activity will finish immediately, regardless of
    // the length of the toast, and the message will not be readable. The new version isn't
    // quite right either because the app will execute Screen.Initialize while the message
    // is being shown.

  }

  // show a notification and kill the app when the button is pressed
  private void showNoticeAndDie(String message, String title, String buttonText){
    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
    alertDialog.setTitle(title);
    // prevents the user from escaping the dialog by hitting the Back button
    alertDialog.setCancelable(false);
    alertDialog.setMessage(message);
    alertDialog.setButton(buttonText, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        activity.finish();
      }});
    alertDialog.show();
  }


  /**
   * Setter for the app developer's API key.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
    @SimpleProperty
    public void ApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    /**
     * Getter for the API key.
     * @return apiKey the apiKey
     */
    @SimpleProperty(
        description = "Your Google API Key. For help, click on the question" +
                        "mark (?) next to the FusiontablesControl component in the Palette. ",
        category = PropertyCategory.BEHAVIOR)
    public String ApiKey() {
      return apiKey;
    }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
    defaultValue = DEFAULT_QUERY)
  @SimpleProperty
  public void Query(String query) {
    this.query = query;
  }

  @SimpleProperty(
      description = "The query to send to the Fusion Tables API. " +
      "<p>For legal query formats and examples, see the " +
      "<a href=\"https://developers.google.com/fusiontables/docs/v1/getting_started\" target=\"_blank\">Fusion Tables API v1.0 reference manual</a>.</p> " +
      "<p>Note that you do not need to worry about UTF-encoding the query. " +
      "But you do need to make sure it follows the syntax described in the reference manual, " +
      "which means that things like capitalization for names of columns matters, " +
      "and that single quotes need to be used around column names if there are spaces in them.</p> ",
      category = PropertyCategory.BEHAVIOR)
  public String Query() {
    return query;
  }

  /**
   * Calls QueryProcessor to execute the API request asynchronously, if
   * the user has already authenticated with the Fusiontables service.
   */
  @SimpleFunction(description = "Send the query to the Fusiontables server.")
  public void SendQuery() {
    new QueryProcessorV1(activity).execute(query);
  }

//Deprecated  -- Won't work after 12/2012
  @SimpleFunction(description = "DEPRECATED. This block " +
       "will be deprecated by the end of 2012.  Use SendQuery.")
  public void DoQuery() {
    if (requestHelper != null) {
      new QueryProcessor().execute(query);
    } else {
      form.dispatchErrorOccurredEvent(this, "DoQuery",
          ErrorMessages.ERROR_FUNCTIONALITY_NOT_SUPPORTED_FUSIONTABLES_CONTROL);
    }
  }

  @SimpleEvent(
      description = "Indicates that the Fusion Tables query has finished processing, " +
        "with a result.  The result of the query will generally be returned in CSV format, and " +
        "can be converted to list format using the \"list from csv table\" or " +
      "\"list from csv row\" blocks.")
  public void GotResult(String result) {
    // Invoke the application's "GotValue" event handler
    EventDispatcher.dispatchEvent(this, "GotResult", result);
  }

  // TODO(sharon): figure out why this isn't working
  // TODO(ralph): Looks like it's working for OAuth 2, Let's user switch accounts
  @SimpleFunction
  public void ForgetLogin() {
    OAuth2Helper.resetAccountCredential(activity);
  }


  // To be Deprecated, based on the old API
  private IClientLoginHelper createClientLoginHelper(String accountPrompt, String service) {
    if (SdkLevel.getLevel() >= SdkLevel.LEVEL_ECLAIR) {
      HttpClient httpClient = new DefaultHttpClient();
      HttpConnectionParams.setSoTimeout(httpClient.getParams(), SERVER_TIMEOUT_MS);
      HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), SERVER_TIMEOUT_MS);
      return new ClientLoginHelper(activity, service, accountPrompt, httpClient);
    }
    return null;
  }

  /**
   * Generate a FusionTables POST request
   */
  // To be deprecated, based on the old API
  private HttpUriRequest genFusiontablesQuery(String query) throws IOException {
    HttpPost request = new HttpPost(FUSION_QUERY_URL);
    ArrayList<BasicNameValuePair> pair = new ArrayList<BasicNameValuePair>(1);
    pair.add(new BasicNameValuePair("sql", query));
    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(pair, "UTF-8");
    entity.setContentType("application/x-www-form-urlencoded");
    request.setEntity(entity);
    return request;
  }

  /**
   * To be deprecated -- will no longer after 12/2012.
   * Sends the Fusiontables request asynchronously to the server and returns the result.
   * This version uses the Deprecated SQL API.
   */
  private class QueryProcessor extends AsyncTask<String, Void, String> {
    private ProgressDialog progress = null;

    @Override
    protected void onPreExecute() {
      progress = ProgressDialog.show(activity, "Fusiontables", "processing query...", true);
    }

    /**
     * Query the fusiontables server.
     * @return The resulant table, error page, or exception message.
     */
    @Override
    protected String doInBackground(String... params) {
      try {
        HttpUriRequest request = genFusiontablesQuery(params[0]);
        Log.d(LOG_TAG, "Fetching: " + params[0]);
        HttpResponse response = requestHelper.execute(request);
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        response.getEntity().writeTo(outstream);
        Log.d(LOG_TAG, "Response: " + response.getStatusLine().toString());
        return outstream.toString();
      } catch (IOException e) {
        e.printStackTrace();
        return e.getMessage();
      }
    }

    /**
     * Got the results.  We could parse the CSV and do something useful with it.
     */

    @Override
    protected void onPostExecute(String result) {
      progress.dismiss();
      GotResult(result);
    }
  }

  /**
   * Executes a Fusiontable query with an OAuth 2.0 authenticated
   * request.  Requests are authenticated by attaching an
   * Authentication header to the Http request.  The header
   * takes the form 'Authentication Oauth <access_token>'.
   *
   * Requests take the form of SQL strings, using an Sql
   * object from the Google API Client library.  Apparently
   * the Sql object handles the decision of whether the request
   * should be a GET or a POST.  Queries such as 'show tables'
   * and 'select' are supposed to be GETs and queries such as
   * 'insert' are supposed to be POSTS.
   *
   * See <a href="https://developers.google.com/fusiontables/docs/v1/using">https://developers.google.com/fusiontables/docs/v1/using</a>
   *
   * @param query the raw SQL string used by App Inventor
   * @param authToken the OAuth 2.0 access token
   * @return the HttpResponse if the request succeeded, or null
   */
  public com.google.api.client.http.HttpResponse sendQuery(String query, String authToken) {
    Log.i(LOG_TAG, "executing " + query);
    com.google.api.client.http.HttpResponse response = null;

    // Create a Fusiontables service object (from Google API client lib)
    Fusiontables service = new Fusiontables.Builder(
          AndroidHttp.newCompatibleTransport(),
          new GsonFactory(),
          new GoogleCredential())
    .setApplicationName("App Inventor Fusiontables/v1.0")
    .setJsonHttpRequestInitializer(new GoogleKeyInitializer(ApiKey()))
    .build();

    try {

      // Construct the SQL query and get a CSV result
      Sql sql =
        ((Fusiontables) service).query().sql(query);
      sql.put("alt", "csv");

      // Add the authToken to authentication header
      sql.setOauthToken(authToken);

      response = sql.executeUnparsed();

    } catch (GoogleJsonResponseException e) {
      e.printStackTrace();
      errorMessage = e.getMessage();
    } catch (IOException e) {
      e.printStackTrace();
      errorMessage = e.getMessage();
    }
    return response;
  }

  /**
   * Static utility method to prettify the HttpResponse. This version uses Google API
   * HttpResponse object, which is different than Apache's
   * @param response
   * @return resultString
   */
  public static String httpResponseToString(com.google.api.client.http.HttpResponse response) {
    String resultStr = "";
    if (response != null) {
      if (response.getStatusCode() != 200) {
        resultStr = response.getStatusCode() + " " + response.getStatusMessage();
      } else {
        try {
          resultStr = parseResponse(response.getContent());
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
    return resultStr;
  }

  /**
   * Handles Apache Http Response.  Uses Apache's HttpResponse object, which is different
   * from Google's.
   * @param response
   * @return The result string
   */
  public static String httpApacheResponseToString(org.apache.http.HttpResponse response) {
    String resultStr = "";
    if (response != null) {
      if (response.getStatusLine().getStatusCode() != 200) {
        resultStr = response.getStatusLine().getStatusCode() + " "
          + response.getStatusLine().getReasonPhrase();
      } else {
        try {
          resultStr = parseResponse(response.getEntity().getContent());
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return resultStr;
  }

  /**
   * Parses the input stream returned from Http query
   * @param input
   * @return The Result String
   */
  public static String parseResponse(InputStream input) {
    String resultStr = "";
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(input));

      StringBuilder sb = new StringBuilder();

      String line;
      while ((line = br.readLine()) != null) {
        sb.append(line + "\n");
      }
      resultStr = sb.toString();
      Log.i(LOG_TAG, "resultStr = " + resultStr);
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return resultStr;
  }


  /**
   * Callback used for error reporting.
   * @param msg
   */
  public void handleOAuthError(String msg) {
    Log.i(LOG_TAG, "handleOAuthError: " + msg);
    errorMessage = msg;
  }

  /**
   * Parses SQL API Create query into v1.0 a JSon string which is then submitted as a POST request
   * E.g., parses "
   *   CREATE TABLE Notes (NoteField: STRING,  NoteLength: NUMBER, Date:DATETIME, Location:LOCATION)"
   * into :
   *  "CREATE TABLE " +
      "{\"columns\": [{\"name\": \"NoteField\",\"type\": \"STRING\"},{\"name\": \"NoteLength\",\"type\": \"NUMBER\"}," +
      "{\"name\": \"Location\",\"type\": \"LOCATION\"},{\"name\": \"Date\",\"type\": \"DATETIME\"}], " +
      "\"isExportable\": \"true\", \"name\": \"Notes\"}"

   * @param query
   * @return
   */
  private String parseSqlCreateQueryToJson (String query) {
    Log.i(LOG_TAG, "parsetoJSonSqlCreate :" + query);
    StringBuilder jsonContent = new StringBuilder();
    query = query.trim();
    String tableName = query.substring("create table".length(), query.indexOf('(')).trim();
    String columnsList = query.substring(query.indexOf('(') + 1, query.indexOf(')'));
    String [] columnSpecs = columnsList.split(",");
    jsonContent.append("{'columns':[");
    for (int k = 0; k < columnSpecs.length; k++) {
      String [] nameTypePair = columnSpecs[k].split(":");
      jsonContent.append("{'name': '" + nameTypePair[0].trim() + "', 'type': '" + nameTypePair[1].trim() + "'}" );
      if (k < columnSpecs.length -1) {
        jsonContent.append(",");
      }
    }
    jsonContent.append("],");
    jsonContent.append("'isExportable':'true',");
    jsonContent.append("'name': '" + tableName + "'");
    jsonContent.append("}");

    jsonContent.insert(0, "CREATE TABLE ");

    Log.i(LOG_TAG, "result = " + jsonContent.toString());
    return jsonContent.toString();
  }

  /**
   * Method for handling 'create table' SQL queries. At this point that is
   * the only query that we support using a POST request.
   *
   * TODO: Generalize this for other queries that require POST.
   *
   * @param query -- a query of the form "create table <json encoded content>"
   * @param authToken -- Oauth 2.0 access token
   * @return
   */
  private String doPostRequest(String query, String authToken) {
    org.apache.http.HttpResponse response = null;
    String jsonContent = query.trim().substring("create table".length());
    Log.i(LOG_TAG, "Http Post content = " + jsonContent);

    // Set up the POST request

    StringEntity entity = null;
    HttpPost request = new HttpPost(FUSIONTABLES_POST + "?key=" + ApiKey()); // Fusiontables Uri
    try {
      entity = new StringEntity(jsonContent);
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    }
    entity.setContentType("application/json");
    request.addHeader("Authorization", AUTHORIZATION_HEADER_PREFIX  + authToken);
    request.setEntity(entity);

    // Execute the request

    HttpClient client = new DefaultHttpClient();
    try {
      response = client.execute(request);
    } catch (ClientProtocolException e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    } catch (IOException e) {
      e.printStackTrace();
      return "Error: " + e.getMessage();
    }

    // Process the response
    // A valid response will have code=200 and contain a tableId value plus other stuff.
    // We just return the table id.
    int statusCode = response.getStatusLine().getStatusCode();
    if (response != null && statusCode == 200) {
      try {
        String jsonResult =  FusiontablesControl.httpApacheResponseToString(response);
        JSONObject jsonObj = new JSONObject(jsonResult);
        if (jsonObj.has("tableId")) {
          queryResultStr = "tableId," + jsonObj.get("tableId");
        } else {
          queryResultStr = jsonResult;
        }

      } catch (IllegalStateException e) {
        e.printStackTrace();
        return "Error: " + e.getMessage();
      } catch (JSONException e) {
        e.printStackTrace();
        return "Error: " + e.getMessage();
      }
      Log.i(LOG_TAG, "Response code = " + response.getStatusLine());
      Log.i(LOG_TAG, "Query = " + query + "\nResultStr = " + queryResultStr);
      // queryResultStr = response.getStatusLine().toString();
    } else {
      Log.i(LOG_TAG, "Error: " + response.getStatusLine().toString());
      queryResultStr = response.getStatusLine().toString();
    }

    return queryResultStr;
  }


  /**
   * First uses OAuth2Helper to acquire an access token and then sends the
   * Fusiontables query asynchronously to the server and returns the result.
   *
   * This version uses the Fusion Tabes V1.0 API.
   */
  private class QueryProcessorV1 extends AsyncTask<String, Void, String> {
    private static final String TAG = "QueryProcessorV1";

    private final Activity activity; // The main list activity
    private final ProgressDialog dialog;

    /**
     * @param activity, needed to create a progress dialog
     */
    QueryProcessorV1(Activity activity) {
      Log.i(TAG, "Creating AsyncFusiontablesQuery");
      this.activity = activity;
      dialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
      dialog.setMessage("Fusiontables...");
      dialog.show();
    }

    /**
     * The Oauth handshake and the API request are both handled here.
     */
    @Override
    protected String doInBackground(String... params) {
      Log.i(TAG, "Starting doInBackground " + params[0]);
      String query = params[0];
      queryResultStr = "";

      // Get a fresh access token
      OAuth2Helper oauthHelper = new OAuth2Helper();
      String authToken = oauthHelper.getRefreshedAuthToken(activity, authTokenType);

      // Make the fusiontables query

      if (authToken != null) {

        // We handle CREATE TABLE as a special case
        if (query.toLowerCase().contains("create table")) {
          queryResultStr = doPostRequest(parseSqlCreateQueryToJson(query), authToken);
          return queryResultStr;
        } else {

          // Execute all other queries
          com.google.api.client.http.HttpResponse response = sendQuery(query, authToken);

          // Process the response
          if (response != null) {
            queryResultStr = httpResponseToString(response);
            Log.i(TAG, "Query = " + query + "\nResultStr = " + queryResultStr);
          } else {
            queryResultStr = errorMessage;
            Log.i(TAG, "Error:  " + errorMessage);
          }
          return queryResultStr;
        }
      } else {
        return OAuth2Helper.getErrorMessage();
      }
    }

    /**
     * Fires the AppInventor GotResult() method
     */
    @Override
    protected void onPostExecute(String result) {
      Log.i(LOG_TAG, "Query result " + result);
      if (result == null) {
        result = "Error";
      }
      dialog.dismiss();
      GotResult(result);
   }
  }

}
