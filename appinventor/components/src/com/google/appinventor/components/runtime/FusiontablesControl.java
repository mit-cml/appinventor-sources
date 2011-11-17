// Copyright 2011 Google Inc. All Rights Reserved.
package com.google.appinventor.components.runtime;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ClientLoginHelper;
import com.google.appinventor.components.runtime.util.ErrorMessages;
import com.google.appinventor.components.runtime.util.IClientLoginHelper;
import com.google.appinventor.components.runtime.util.SdkLevel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Appinventor fusiontables control
 */
@DesignerComponent(version = YaVersion.FUSIONTABLESCONTROL_COMPONENT_VERSION,
    description = "<p>A non-visible component that communicates with Google Fusion Tables. " +
      "Fusion Tables lets you store, share, query and visualize data tables; " +
      "this component lets you query, create, and modify these tables, using the " +
      "<a href=\"http://code.google.com/apis/fusiontables/docs/developers_reference.html\"" +
      " target=\"_blank\">Fusion Tables SQL API</a>.</p>" +
      "<p>To use the component, define a query, call DoQuery to execute the query, " +
       "and GotResult will hand you the result when it is ready.</p>" +
      "<p>Note that you do not need to worry about encoding the query, " +
      "but you do have to make sure it follows the syntax described in the reference manual, " +
      "which means that things like capitalization for names of columns matters, and " +
      "that single quotes need to be used around column names if there are spaces in them.</p>" +
      "<p>The results of the query will generally be returned in CSV format, and " +
      "can be converted to list format using the \"list from csv table\" or " +
      "\"list from csv row\" blocks.</p>",
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
public class FusiontablesControl extends AndroidNonvisibleComponent implements Component {
  private static final String LOG_TAG = "fusion";
  private static final String DIALOG_TEXT = "Choose an account to access FusionTables";
  private static final String FUSION_QUERY_URL = "http://www.google.com/fusiontables/api/query";
  private static final String DEFAULT_QUERY = "show tables";
  private static final String FUSIONTABLES_SERVICE = "fusiontables";
  private static final int SERVER_TIMEOUT_MS = 30000;

  private final Activity activity;
  private final IClientLoginHelper requestHelper;
  private String query;

  public FusiontablesControl(ComponentContainer componentContainer) {
    super(componentContainer.$form());
    this.activity = componentContainer.$context();
    requestHelper = createClientLoginHelper(DIALOG_TEXT, FUSIONTABLES_SERVICE);
    query = DEFAULT_QUERY;
  }

  @DesignerProperty(editorType = DesignerProperty.PROPERTY_TYPE_STRING,
    defaultValue = DEFAULT_QUERY)
  @SimpleProperty
  public void Query(String query) {
    this.query = query;
  }

  @SimpleProperty(
      description = "The query to send to the Fusion Tables API. " +
      "<p>For legal query formats and examples, see the " +
      "Fusion Tables SQL API reference manual. " +
      "<p>Note that you do not need to worry about encoding the query, " +
      "but you do have to make sure it follows the syntax described in the reference manual, " +
      "which means that things like capitalization for names of columns matters, " +
      "and that single quotes need to be used around column names if there are spaces in them. ",
      category = PropertyCategory.BEHAVIOR)
  public String Query() {
    return query;
  }

  @SimpleFunction(description = "Send the query to the Fusion Tables server.")
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

  /* TODO(sharon): figure out why this isn't working
  @SimpleFunction
  public void ForgetLogin() {
    if (requestHelper != null) {
      requestHelper.forgetAccountName();
    }
  }
  */

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
   * Send the fusiontables request to the server and get back the results.
   *
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
      // (result.stqueryartsWith("<HTML>") ? Html.fromHtml(result) : result);
    }
  }
}
