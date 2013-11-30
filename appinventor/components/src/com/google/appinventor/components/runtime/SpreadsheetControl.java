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
import com.google.gdata.client.spreadsheet.SpreadsheetQuery;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
    category = ComponentCategory.STORAGE,
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
    "gdata-client-1.0.jar,"+
    "gdata-client-meta-1.0.jar,"+
    "gdata-core-1.0.jar,"+
    "gdata-spreadsheet-3.0.jar,"+
    "gdata-spreadsheet-meta-3.0.jar,"+
    "guava-14.0.1.jar")

public class SpreadsheetControl extends AndroidNonvisibleComponent implements Component {
  private static final String LOG_TAG = "spreadsheet";
  private static final String DIALOG_TEXT = "Choose an account to access Spreadsheets";
//  private static final String FUSION_QUERY_URL = "http://www.google.com/fusiontables/api/query";
//  public static final String FUSIONTABLES_POST = "https://www.googleapis.com/fusiontables/v1/tables";

//  private static final String DEFAULT_QUERY = "show tables";
  private static final String FUSIONTABLES_SERVICE = "fusiontables";
  private static final int SERVER_TIMEOUT_MS = 30000;
  public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";


  public static final String FUSIONTABLES_URL = "https://www.googleapis.com/fusiontables/v1/query";
  public static final String AUTH_TOKEN_TYPE_SPREADSHEETS = "oauth2:https://spreadsheets.google.com/feeds";
  public static final String APP_NAME = "App Inventor";

  private String authTokenType = AUTH_TOKEN_TYPE_SPREADSHEETS;

  /**
   * The developer's Google API key,
   * See <a href="https://code.google.com/apis/console/">https://code.google.com/apis/console/</a>
   */
  private String cellID;
  private String worksheetID;
  private String spreadsheetKey;

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
  private String errorMessage = "Error on Spreadsheets query";

  private final Activity activity;
private String value;
  //private final IClientLoginHelper requestHelper;

  public SpreadsheetControl(ComponentContainer componentContainer) {
    super(componentContainer.$form());
    this.activity = componentContainer.$context();
    //requestHelper = createClientLoginHelper(DIALOG_TEXT, FUSIONTABLES_SERVICE);
   // query = DEFAULT_QUERY;

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
   * Setter for the specific Spreadsheet key.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
    @SimpleProperty
    public void SpreadsheetKey(String spreadsheetKey) {
      this.spreadsheetKey = spreadsheetKey;
    }

  /**
   * Setter for the specific worksheet ID
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
    @SimpleProperty
    public void WorksheetID(String worksheetID) {
      this.worksheetID = worksheetID;
    }
  
  /**
   * Setter for the cell ID
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
    @SimpleProperty
    public void CellID(String cellID) {
      this.cellID = cellID;
    }
  
  /**
   * Setter for the cell value
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
      defaultValue = "")
    @SimpleProperty
    public void Value(String value) {
      this.value = value;
    }
  
    /**
     * Getter for the cell value.
     * @return value the value
     */
    @SimpleProperty(
        description = "Your new Cell Value. For help, click on the question" +
                        "mark (?) next to the SpreadsheetControl component in the Palette. ",
        category = PropertyCategory.BEHAVIOR)
    public String Value() {
    	return this.value;
    }
    /**
     * Getter for the Spreadsheet key.
     * @return spreadsheetKey the spreadsheetKey
     */
    @SimpleProperty(
        description = "Your Spreadsheet Key. For help, click on the question" +
                        "mark (?) next to the SpreadsheetControl component in the Palette. ",
        category = PropertyCategory.BEHAVIOR)
    public String SpreadsheetKey() {
      return this.spreadsheetKey;
    }
    
    /**
     * Getter for the Worksheet ID.
     * @return apiKey the apiKey
     */
    @SimpleProperty(
        description = "Your WorksheetID. For help, click on the question" +
                        "mark (?) next to the SpreadsheetControl component in the Palette. ",
        category = PropertyCategory.BEHAVIOR)
    public String WorksheetID() {
      return this.worksheetID;
    }
    
    /**
     * Getter for the CellID.
     * @return cellID the cellID
     */
    @SimpleProperty(
        description = "Your CellID. For help, click on the question" +
                        "mark (?) next to the SpreadsheetControl component in the Palette. ",
        category = PropertyCategory.BEHAVIOR)
    public String CellID() {
      return this.cellID;
    }

//  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING,
//    defaultValue = "")
//  @SimpleProperty
//  public void CellID(String query) {
//    this.query = query;
//  }

//  @SimpleProperty(
//      description = "The query to send to the Fusion Tables API. " +
//      "<p>For legal query formats and examples, see the " +
//      "<a href=\"https://developers.google.com/fusiontables/docs/v1/getting_started\" target=\"_blank\">Fusion Tables API v1.0 reference manual</a>.</p> " +
//      "<p>Note that you do not need to worry about UTF-encoding the query. " +
//      "But you do need to make sure it follows the syntax described in the reference manual, " +
//      "which means that things like capitalization for names of columns matters, " +
//      "and that single quotes need to be used around column names if there are spaces in them.</p> ",
//      category = PropertyCategory.BEHAVIOR)
//  public String Value() {
//    return ;
//  }

  /**
   * Calls QueryProcessorV1 to update the cell asynchronously, if
   * the user has already authenticated with the Google Spreadsheet service.
   */
  @SimpleFunction(description = "Send the updated value to the Google Spreadsheet")
  public void UpdateCell() {
    new QueryProcessorV1(activity).execute(this.value, this.cellID);
  }

  /**
   * Calls QueryProcessorV1 to clear the cell asynchronously, if
   * the user has already authenticated with the Google Spreadsheet service.
   */
  @SimpleFunction(description = "Clear the specified cell in the Google Spreadsheet")
  public void ClearCell() {
    new QueryProcessorV1(activity).execute("", this.cellID);
  }
  //Don't Touch
  @SimpleEvent(
      description = "Indicates that the Spreadsheet query has finished processing, " +
        "with a result.")
  public void GotResult(String result) {
    // Invoke the application's "GotValue" event handler
    EventDispatcher.dispatchEvent(this, "GotResult", result);
  }


  // TODO(sharon): figure out why this isn't working
  // TODO(ralph): Looks like it's working for OAuth 2, Let's user switch accounts
  //Don't Touch
  @SimpleFunction
  public void ForgetLogin() {
    OAuth2Helper.resetAccountCredential(activity);
  }


  // To be Deprecated, based on the old API
  //Don't Touch
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
   * @return the CellFeed of the specific worksheet/spreadsheet
   */
  public CellFeed getCellFeed(String authToken) {
    com.google.api.client.http.HttpResponse response = null;

    SpreadsheetQuery hello = new SpreadsheetQuery(null);

    SpreadsheetService service = new SpreadsheetService("SpreadsheetIntegration");
    service.setHeader("Authorization", "Bearer " + authToken);
    
 // Define the URL to request.  This should never change.
    URL SPREADSHEET_FEED_URL = null;
	try {
		String url = "https://spreadsheets.google.com/feeds/spreadsheets/"+this.spreadsheetKey;

		SPREADSHEET_FEED_URL = new URL(url);
		Log.i("URL Spreadsheet", url);
		} catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

 
    SpreadsheetEntry spreadsheet = null;
	try {
		spreadsheet = service.getEntry(SPREADSHEET_FEED_URL, SpreadsheetEntry.class);
	} catch (IOException e2) {
		// TODO Auto-generated catch block
		e2.printStackTrace();
		Log.e("Spreadsheet Entry IOEx", SPREADSHEET_FEED_URL.toString());

	} catch (ServiceException e2) {
		// TODO Auto-generated catch block
		Log.e("Spreadsheet Entry ServEx", SPREADSHEET_FEED_URL.toString());

		e2.printStackTrace();
	}//spreadsheets.get(0);
	if (spreadsheet == null)
		Log.e("Spreadsheet Entry null", SPREADSHEET_FEED_URL.toString());

    //System.out.println(spreadsheet.getTitle().getPlainText());
    Log.i("Spreadsheet Name", spreadsheet.getTitle().getPlainText());
    // Get the first worksheet of the first spreadsheet.
    // TODO: Choose a worksheet more intelligently based on your
    // app's needs.
    WorksheetFeed worksheetFeed = null;
	try {
		worksheetFeed = service.getFeed(
			    spreadsheet.getWorksheetFeedUrl(), WorksheetFeed.class);
	} catch (IOException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	} catch (ServiceException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
    List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
    WorksheetEntry worksheet = worksheets.get(Integer.valueOf(worksheetID));

    // Fetch the cell feed of the worksheet.
    URL cellFeedUrl = worksheet.getCellFeedUrl();
    CellFeed cellFeed = null;
    try {
		cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (ServiceException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

    return cellFeed;
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
      Log.i(TAG, "Creating AsyncSpreadsheetQuery");
      this.activity = activity;
      dialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
//      dialog.setMessage("Spreadsheets...");
//      dialog.show();
    }

    /**
     * The Oauth handshake and the API request are both handled here.
     */
    @Override
    protected String doInBackground(String... params) {
      Log.i(TAG, "Starting doInBackground " + params[1]);
      String value = params[0];
      String cellID = params[1];
      queryResultStr = "";

      // Get a fresh access token
      OAuth2Helper oauthHelper = new OAuth2Helper();
      String authToken = oauthHelper.getRefreshedAuthToken(activity, authTokenType);

      // Make the fusiontables query

      if (authToken != null) {
    	  CellFeed cellFeed = getCellFeed(authToken);
    	  
    	  for (CellEntry cell : cellFeed.getEntries()){
    		  if (cell.getTitle().getPlainText().equals(cellID)){
    			 cell.changeInputValueLocal(value);
    			 
					CellEntry ce;
					try {
						ce = cell.update();
						return ce.getCell().getValue();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e("Cell update IOException", "didn't work");

					} catch (ServiceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Log.e("Cell update Service Exception", "didn't work");

					}
				
    		  }
    		  
    	  } 
    	  return "Cell not Found";
/*        // We handle CREATE TABLE as a special case
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
          return queryResultStr;}
      */
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
