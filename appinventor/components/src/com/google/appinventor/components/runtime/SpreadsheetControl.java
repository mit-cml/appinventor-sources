// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt
package com.google.appinventor.components.runtime;

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
import com.google.appinventor.components.runtime.errors.YailRuntimeError;
import com.google.appinventor.components.runtime.util.OAuth2Helper;
import com.google.appinventor.components.runtime.util.SdkLevel;
import com.google.appinventor.components.runtime.util.YailList;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.CustomElementCollection;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Appinventor Spreadsheet control.
 * 
 * See <a
 * href="https://developers.google.com/google-apps/spreadsheets/">https://
 * developers.google.com/google-apps/spreadsheets/</a>
 * 
 * The main change occurs in the way API requests are authorized. This version
 * uses OAuth 2.0 and makes use of OAuth2Helper. The helper uses the Google
 * AccountManager to acquire an access token that must be attached as the OAuth
 * header in all Spreadsheets requests.
 * 
 * Before a Spreadsheet request can be made, the app must acquire an OAuth
 * token. This may involve the user logging in to their Gmail account (or not if
 * they are already logged in) and then being prompted to give the app
 * permission to access the user's spreadsheets.
 * 
 * Permission takes the form of an access token (called authToken), which must
 * be transmitted to the Spreadsheets service as part of all requests.
 * 
 */
@DesignerComponent(version = YaVersion.SPREADSHEETSCONTROL_COMPONENT_VERSION, description = "<p>A non-visible component that communicates with Google Spreadsheets. "
    + "Spreadsheets "
    + "this component lets you modify information within a Google Spreadsheet, including</p> "
    + "<p>This component uses the "
    + "<a href=\"https://developers.google.com/google-apps/spreadsheets\" target=\"_blank\">Google Spreadsheets API</a>. "
    + "<p>In order to develop apps that use Spreadsheets, you must obtain an Spreadsheets Key."
    + "<p>To get an Spreadsheets key, follow these instructions.</p> "
    + "<ol>"
    + "<li>Go to your Google Drive login if necessary.</li>"
    + "<li>Select/Create the relevant Spreadsheet item from the menu on the left.</li>"
    + "<li>The Spreadsheets key will be placed in the URL immediately after <i> key=</i></li>", category = ComponentCategory.STORAGE, nonVisible = true, iconName = "images/spreadsheets.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET,"
    + "android.permission.ACCOUNT_MANAGER,"
    + "android.permission.MANAGE_ACCOUNTS,"
    + "android.permission.GET_ACCOUNTS,"
    + "android.permission.USE_CREDENTIALS")
@UsesLibraries(libraries = "google-api-client-beta.jar,"
    + "google-api-client-android2-beta.jar,"
    + "google-http-client-beta.jar,"
    + "google-http-client-android2-beta.jar,"
    + "google-http-client-android3-beta.jar,"
    + "google-oauth-client-beta.jar," + "gdata-client-1.0.jar,"
    + "gdata-client-meta-1.0.jar," + "gdata-core-1.0.jar,"
    + "gdata-spreadsheet-3.0.jar," + "gdata-spreadsheet-meta-3.0.jar,"
    + "guava-14.0.1.jar")
public class SpreadsheetControl extends AndroidNonvisibleComponent implements
Component {
  private static final String LOG_TAG = "spreadsheet";
  public static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

  public static final String AUTH_TOKEN_TYPE_SPREADSHEETS = "oauth2:https://spreadsheets.google.com/feeds";
  public static final String APP_NAME = "App Inventor";

  private String authTokenType = AUTH_TOKEN_TYPE_SPREADSHEETS;

  private String spreadsheetKey;
  private SpreadsheetEntry spreadsheet;
  private SpreadsheetService service;
  private URL SPREADSHEET_FEED_URL;
  private final Activity activity;
  private String[] headings;

  private enum SpreadsheetAction {
    SET_SPREADSHEET, CREATE_WORKSHEET, RESIZE_WORKSHEET, RENAME_WORKSHEET, DELETE_WORKSHEET, GET_WORKSHEET_ROW, GET_WORKSHEET_COL, ADD_ROW, UPDATE_ROW, DELETE_ROW, UPDATE_CELL, CLEAR_CELL, GET_CELL;
  };

  public SpreadsheetControl(ComponentContainer componentContainer) {
    super(componentContainer.$form());
    this.activity = componentContainer.$context();

    if (SdkLevel.getLevel() < SdkLevel.LEVEL_ECLAIR) {
      showNoticeAndDie(
          "Sorry. The Spreadsheets component is not compatible with this phone.",
          "This application must exit.", "Rats!");
    }
  }

  // show a notification and kill the app when the button is pressed
  private void showNoticeAndDie(String message, String title,
      String buttonText) {
    AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
    alertDialog.setTitle(title);
    // prevents the user from escaping the dialog by hitting the Back button
    alertDialog.setCancelable(false);
    alertDialog.setMessage(message);
    alertDialog.setButton(buttonText,
        new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int which) {
        activity.finish();
      }
    });
    alertDialog.show();
  }

  /**
   * Setter for the specific Spreadsheet key.
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void SpreadsheetKey(String spreadsheetKey) {
    this.spreadsheetKey = spreadsheetKey;
    new QueryProcessorV1(activity)
    .execute(SpreadsheetAction.SET_SPREADSHEET.name());
  }


  /**
   * Method to retrieve the current Spreadsheet key.
   * 
   * @return spreadsheetKey 
   */
  @SimpleProperty(description = "Your Spreadsheet Key. For help, click on the question"
      + "mark (?) next to the SpreadsheetControl component in the Palette. ", category = PropertyCategory.BEHAVIOR)
  public String SpreadsheetKey() {
    return this.spreadsheetKey;
  }

  

  /**
   * Calls QueryProcessorV1 to update the cell asynchronously, if the user has
   * already authenticated with the Google Spreadsheet service.
   */
  @SimpleFunction(description = "Send the updated value to the Google Spreadsheet")
  public void UpdateCell(String worksheetTitle, String cellID, String value) {
    new QueryProcessorV1(activity).execute(
        SpreadsheetAction.UPDATE_CELL.name(), worksheetTitle, cellID,
        value);
  }

  /**
   * Calls QueryProcessorV1 to clear the cell asynchronously, if the user has
   * already authenticated with the Google Spreadsheet service.
   */
  @SimpleFunction(description = "Clear the specified cell in the Google Spreadsheet")
  public void ClearCell(String worksheetTitle, String cellID) {
    new QueryProcessorV1(activity)
    .execute(SpreadsheetAction.CLEAR_CELL.name(), worksheetTitle,
        cellID, "");
  }

  /**
   * Calls QueryProcessorV1 to get the cell value asynchronously, if the user has
   * already authenticated with the Google Spreadsheet service.
   */
  @SimpleFunction(description = "Get value of cell, if it exists, in existing worksheet")
  public void GetCellValue(String worksheetTitle, String cellID) {
    new QueryProcessorV1(activity).execute(
        SpreadsheetAction.GET_CELL.name(), worksheetTitle, cellID, "");
  }

  @SimpleEvent(description = "Indicates that the Cell Value has been retrieved, "
      + "with a result.")
  public void GotCellValue(String result) {
    // Invoke the application's "GotCellValue" event handler
    EventDispatcher.dispatchEvent(this, "GotCellValue", result);
  }
  
  
  @SimpleEvent(description = "Indicates that the Worksheet query has been retrieved, "
      + "with a result.")
  public void GotColumnCount(String result) {
    // Invoke the application's "GotCellValue" event handler
    EventDispatcher.dispatchEvent(this, "GotColumnCount", result);
  }
  
  @SimpleEvent(description = "Indicates that the Worksheet query has been retrieved, "
	      + "with a result.")
	  public void GotRowCount(String result) {
	    // Invoke the application's "GotRowalue" event handler
	    EventDispatcher.dispatchEvent(this, "GotRowCount", result);
	  }

  /**
   * Calls QueryProcessorV1 to create a worksheet asynchronously, if the user has
   * already authenticated with the Google Spreadsheet service.
   */
  @SimpleFunction(description = "Create a new worksheet in the Google Spreadsheet")
  public void CreateWorksheet(String newWorksheetTitle, int rowCount,
      int columnCount) {
    new QueryProcessorV1(activity).execute(
        SpreadsheetAction.CREATE_WORKSHEET.name(),
        String.valueOf(rowCount), String.valueOf(columnCount),
        newWorksheetTitle);
  }

  /**
   * Calls QueryProcessorV1 to resize a worksheet asynchronously, if the user has
   * already authenticated with the Google Spreadsheet service.
   */
  @SimpleFunction(description = "Resize an existing worksheet in the Google Spreadsheet")
  public void ResizeWorksheet(String worksheetTitle, int rowCount,
      int columnCount) {
    new QueryProcessorV1(activity).execute(
        SpreadsheetAction.RESIZE_WORKSHEET.name(),
        String.valueOf(rowCount), String.valueOf(columnCount),
        worksheetTitle);
  }

  /**
   * Calls QueryProcessorV1 to rename a worksheet asynchronously, if the user has
   * already authenticated with the Google Spreadsheet service.
   */
  @SimpleFunction(description = "Rename an existing worksheet in the Google Spreadsheet")
  public void RenameWorksheet(String oldWorksheetTitle,
      String newWorksheetTitle) {
    new QueryProcessorV1(activity).execute(
        SpreadsheetAction.RENAME_WORKSHEET.name(), oldWorksheetTitle,
        newWorksheetTitle);
  }

  /**
   * Calls QueryProcessorV1 to Delete a worksheet asynchronously, if the user has
   * already authenticated with the Google Spreadsheet service.
   */
  @SimpleFunction(description = "Delete an existing worksheet in the Google Spreadsheet")
  public void DeleteWorksheet(String worksheetTitle) {
    new QueryProcessorV1(activity).execute(
        SpreadsheetAction.DELETE_WORKSHEET.name(), worksheetTitle);
  }

  /**
   * Calls QueryProcessorV1 to retrieve a worksheet's row count asynchronously, if the user has
   * already authenticated with the Google Spreadsheet service.
   */
  @SimpleFunction(description = "Get Row count in existing worksheet")
  public void GetWorksheetRowCount(String worksheetTitle) {
    new QueryProcessorV1(activity).execute(
        SpreadsheetAction.GET_WORKSHEET_ROW.name(), worksheetTitle);
  }

  /**
   * Calls QueryProcessorV1 to retrieve a worksheet's column count asynchronously, if the user has
   * already authenticated with the Google Spreadsheet service.
   */
  @SimpleFunction(description = "Get Column count in existing worksheet")
  public void GetWorksheetColCount(String worksheetTitle) {
    new QueryProcessorV1(activity).execute(
        SpreadsheetAction.GET_WORKSHEET_ROW.name(), worksheetTitle);
  }

  /**
   * Elements property setter method
   * 
   * @param itemList
   *            - a YailList containing the strings representing headings
   */
  @SimpleProperty(description = "Column Headings of the worksheet"
  		+ "for which you wish to enter information in records. For help, click on the question"
	      + "mark (?) next to the SpreadsheetControl component in the Palette. ", category = PropertyCategory.BEHAVIOR)
  public void Headings(YailList itemList) {
    Object[] objects = itemList.toStringArray();
    for (int i = 0; i < objects.length; i++) {
      if (!(objects[i] instanceof String)) {
        throw new YailRuntimeError(
            "Headings passed to SpreadsheetControl must be Strings",
            "Error");
      }
    }
    headings = itemList.toStringArray();
  }

  @SimpleFunction(description = "Add a new record of information into an existing worksheet: "
      + "provide a list of 2-element lists--the first the column heading, the second the value")
  public void AppendWorksheetRecord(String worksheetTitle, YailList record) {
    String csvRecord = checkRecordAndGenerateCSV(record);
    new QueryProcessorV1(activity).execute(
        SpreadsheetAction.ADD_ROW.name(), worksheetTitle, csvRecord);
  }

  @SimpleFunction(description = "Update a record of information into an existing worksheet: provide a list of 2-element lists--the first the column heading, the second the value")
  public void UpdateWorksheetRecord(String worksheetTitle, YailList record,
      String heading, String key) {
    String csvRecord = checkRecordAndGenerateCSV(record);
    new QueryProcessorV1(activity).execute(
        SpreadsheetAction.UPDATE_ROW.name(), worksheetTitle, csvRecord,
        heading, key);
  }

  @SimpleFunction(description = "Delete a record of information into an existing worksheet")
  public void DeleteWorksheetRecord(String worksheetTitle, String heading,
      String key) {
    new QueryProcessorV1(activity).execute(
        SpreadsheetAction.DELETE_ROW.name(), worksheetTitle, heading,
        key);
  }

  /**
   * Turns a Yaillist record into a single String csv to be parsed
   * @param record
   * @return csv String representation
   */
  private String checkRecordAndGenerateCSV(YailList record) {
    StringBuilder csvRecord = new StringBuilder();

    Object[] items = record.toStringArray();
    if (items.length != headings.length) {
      throw new YailRuntimeError("Invalid records passed", "Error");
    }
    for (int j = 0; j < items.length; j++) {
      if (!(items[j] instanceof String)) {
        throw new YailRuntimeError(
            "Items passed to SpreadsheetControl within List must be Strings",
            "Error");
      } else {
        csvRecord.append((String) items[j]);
        if (j < items.length - 1)
          csvRecord.append(",");
      }
    }
    return csvRecord.toString();
  }

  // Don't Touch
  @SimpleFunction
  public void ForgetLogin() {
    OAuth2Helper.resetAccountCredential(activity);
  }

  /**
   * Creates a Spreadsheet Service using previously
   * generated OAuth 2.0 access token, and gains
   * the user's Spreadsheet feed.
   * Also saves specific Spreadsheet Entry, if exists and
   * user has access to it.
   * 
   * @param authToken
   *            the OAuth 2.0 access token
   */
  private void setSpreadsheet(String authToken) {
    this.service = new SpreadsheetService("SpreadsheetIntegration");
    this.service.setHeader("Authorization", "Bearer " + authToken);
    // Define the URL to request. This should never change.
    this.SPREADSHEET_FEED_URL = null;
    try {
      String url = "https://spreadsheets.google.com/feeds/spreadsheets/"
          + this.spreadsheetKey;

      this.SPREADSHEET_FEED_URL = new URL(url);
      
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    SpreadsheetEntry spreadsheet = null;
    try {
      spreadsheet = this.service.getEntry(this.SPREADSHEET_FEED_URL,
          SpreadsheetEntry.class);
    } catch (IOException e2) {
      e2.printStackTrace();
      Log.e("Spreadsheet Entry Not Found",
          this.SPREADSHEET_FEED_URL.toString());
    } catch (ServiceException e2) {
      Log.e("Spreadsheet Entry Not Found",
          this.SPREADSHEET_FEED_URL.toString());
      e2.printStackTrace();
    }
    if (spreadsheet == null) {
      Log.e("Spreadsheet Entry null",
          this.SPREADSHEET_FEED_URL.toString());
      throw new YailRuntimeError("Spreadsheet key invalid", "Error");
    }
    this.spreadsheet = spreadsheet;
  }

  /**
   * Locates worksheet with given worksheetTitle
   * within set Spreadsheet, if it exists
   * @param worksheetTitle
   * @return specific WorksheetEntry
   */
  private WorksheetEntry getWorksheet(String worksheetTitle) {

    WorksheetFeed worksheetFeed = null;
    try {
      worksheetFeed = service.getFeed(spreadsheet.getWorksheetFeedUrl(),
          WorksheetFeed.class);
    } catch (IOException e1) {
      Log.e("Worksheet Entry", "Not found");
      e1.printStackTrace();
    } catch (ServiceException e1) {
      Log.e("Worksheet Entry", "Not found");
      e1.printStackTrace();
    }
    WorksheetEntry worksheet = null;
    List<WorksheetEntry> worksheets = worksheetFeed.getEntries();
    for (WorksheetEntry ws : worksheets) {
      if (ws.getTitle().getPlainText().compareTo(worksheetTitle) == 0)
        worksheet = ws;
    }

    if (worksheet == null) {
      Log.e("Worksheet null", SPREADSHEET_FEED_URL.toString());
      throw new YailRuntimeError("Worksheet Title invalid", "Error");
    }
    return worksheet;
  }

  /**
   * Within given WorksheetEntry, returns feed representing
   * Cells within the worksheet, if it exists
   * @param worksheet
   * @param authToken
   * @return
   */
  private CellFeed getCellFeed(WorksheetEntry worksheet, String authToken) {

    // Fetch the cell feed of the worksheet.
    URL cellFeedUrl = worksheet.getCellFeedUrl();
    CellFeed cellFeed = null;
    try {
      cellFeed = service.getFeed(cellFeedUrl, CellFeed.class);
    } catch (IOException e) {
      Log.e("CellFeed", "Not found");
      e.printStackTrace();
    } catch (ServiceException e) {
      Log.e("CellFeed", "Not found");
      e.printStackTrace();
    }

    return cellFeed;

  }

  /**
   * Callback used for error reporting.
   * 
   * @param msg
   */
  private void handleOAuthError(String msg) {
    Log.i(LOG_TAG, "handleOAuthError: " + msg);
  }

  /**
   * First uses OAuth2Helper to acquire an access token and then sends the
   * Spreadsheet Feed query asynchronously to the server and returns the
   * result.
   * 
   * This version uses the Spreadsheets V3.0 API.
   */
  private class QueryProcessorV1 extends AsyncTask<String, Void, String> {
    private static final String TAG = "QueryProcessorV1";

    private final Activity activity; // The main list activity
    private final ProgressDialog dialog;

    /**
     * @param activity
     *            , needed to create a progress dialog
     */
    QueryProcessorV1(Activity activity) {
      Log.i(TAG, "Creating AsyncSpreadsheetQuery");
      this.activity = activity;
      dialog = new ProgressDialog(activity);
    }

    @Override
    protected void onPreExecute() {
    }

    /**
     * The Oauth handshake and the API request are both handled here.
     */
    @Override
    protected String doInBackground(String... params) {
      Log.i(TAG, "Starting doInBackground " + params[0]);
      SpreadsheetAction action = SpreadsheetAction.valueOf(params[0]);
      String output = "Success";
      // Get a fresh access token
      OAuth2Helper oauthHelper = new OAuth2Helper();
      String authToken = oauthHelper.getRefreshedAuthToken(activity,
          authTokenType);

      if (authToken != null) {
        switch (action) {
        case SET_SPREADSHEET:
          setSpreadsheet(authToken);
          break;
        case UPDATE_CELL:
        case CLEAR_CELL:
        case GET_CELL: {
          String worksheetTitle = params[1];
          String cellID = params[2];
          String value = params[3];

          WorksheetEntry worksheet = getWorksheet(worksheetTitle);
          CellFeed cellFeed = getCellFeed(worksheet, authToken);

          for (CellEntry cell : cellFeed.getEntries()) {
            if (cell.getTitle().getPlainText().equals(cellID)) {

              try {
                switch (action) {
                case UPDATE_CELL:
                case CLEAR_CELL: {
                  cell.changeInputValueLocal(value);
                  CellEntry ce = cell.update();
                  // to bypass output reassignment at end
                  return output;}
                case GET_CELL: {
                  // to bypass output reassignment at end
                  return "CELL:" + cell.getCell().getValue();}
                }
              } catch (IOException e) {
                e.printStackTrace();
                Log.e("Cell update IOException", "didn't work");
              } catch (ServiceException e) {
                e.printStackTrace();
                Log.e("Cell update Service Exception",
                    "didn't work");
              }
            }
          }
          output = null;
        }
        break;
        case CREATE_WORKSHEET: {
          int rowCount = Integer.parseInt(params[1]);
          int columnCount = Integer.parseInt(params[2]);
          String newWorksheetTitle = params[3];

          // Create a local representation of the new worksheet.
          WorksheetEntry newWorksheet = new WorksheetEntry();
          newWorksheet.setTitle(new PlainTextConstruct(
              newWorksheetTitle));
          newWorksheet.setColCount(rowCount);
          newWorksheet.setRowCount(columnCount);

          URL worksheetFeedUrl = spreadsheet.getWorksheetFeedUrl();
          try {
            service.insert(worksheetFeedUrl, newWorksheet);
          } catch (IOException e) {
            e.printStackTrace();
            Log.e("Worksheet Insertion", "Failed to insert");
            output = null;
          } catch (ServiceException e) {
            e.printStackTrace();
            Log.e("Worksheet Insertion", "Failed to insert");
            output = null;
          }
        }
        break;
        case DELETE_WORKSHEET: {
          String worksheetName = params[1];
          WorksheetEntry worksheet = getWorksheet(worksheetName);
          try {
            worksheet.delete();
          } catch (IOException e) {
            e.printStackTrace();
            Log.e("Worksheet Deletion", "Failed to delete");
            output = null;
          } catch (ServiceException e) {
            e.printStackTrace();
            Log.e("Worksheet Deletion", "Failed to delete");
            output = null;
          }
        }
        break;
        case RENAME_WORKSHEET: {
          String oldWorksheetName = params[1];
          String newWorksheetName = params[2];
          WorksheetEntry worksheet = getWorksheet(oldWorksheetName);

          worksheet
          .setTitle(new PlainTextConstruct(newWorksheetName));
          // Send the local representation of the worksheet to the API
          // for
          // modification.

          try {
            worksheet.update();
          } catch (IOException e) {
            e.printStackTrace();
            Log.e("Worksheet Rename", "Failed to rename");
            output = null;
          } catch (ServiceException e) {
            e.printStackTrace();
            Log.e("Worksheet Rename", "Failed to delete");
            output = null;
          }
        }
        break;
        case RESIZE_WORKSHEET: {
          int rowCount = Integer.parseInt(params[1]);
          int columnCount = Integer.parseInt(params[2]);
          String worksheetName = params[3];

          WorksheetEntry worksheet = getWorksheet(worksheetName);
          worksheet.setColCount(columnCount);
          worksheet.setRowCount(rowCount);
          // Send the local representation of the worksheet to the API
          // for
          // modification.
          try {
            worksheet.update();
          } catch (IOException e) {
            e.printStackTrace();
            Log.e("Worksheet Resize", "Failed");
            output = null;
          } catch (ServiceException e) {
            e.printStackTrace();
            Log.e("Worksheet Resize", "Failed");
            output = null;
          }
        }
        break;
        case GET_WORKSHEET_ROW: {
          String worksheetName = params[1];
          WorksheetEntry worksheet = getWorksheet(worksheetName);
          output = "ROW:" +String.valueOf(worksheet.getRowCount());
        }
        break;
        case GET_WORKSHEET_COL: {
          String worksheetName = params[1];
          WorksheetEntry worksheet = getWorksheet(worksheetName);
          output = "COL:" + String.valueOf(worksheet.getColCount());
        }
        break;
        case ADD_ROW: {
          String worksheetName = params[1];
          String[] recordList = params[2].split(",");

          WorksheetEntry worksheet = getWorksheet(worksheetName);
          // Define the URL to request the list feed of the worksheet.
          // Fetch the list feed of the worksheet.
          URL listFeedUrl = worksheet.getListFeedUrl();

          ListEntry row = new ListEntry();

          for (int i = 0; i < headings.length; i++) {
            row.getCustomElements().setValueLocal(headings[i],
                recordList[i]);
          }
          // Send the new row to the API for insertion.
          try {
            row = service.insert(listFeedUrl, row);
          } catch (IOException e) {
            Log.e("Add Row", "Failed");
            e.printStackTrace();
            output = null;
            break;
          } catch (ServiceException e) {
            Log.e("Add Row", "Failed");
            e.printStackTrace();
            output = null;
            break;
          }
        }
        break;
        case UPDATE_ROW: {
          String worksheetName = params[1];
          String[] recordList = params[2].split(",");
          String specificHeading = params[3];
          String specificKey = params[4];

          WorksheetEntry worksheet = getWorksheet(worksheetName);
          // Define the URL to request the list feed of the worksheet.
          // Fetch the list feed of the worksheet.
          URL listFeedUrl = worksheet.getListFeedUrl();
          ListFeed listFeed = null;
          try {
            listFeed = service.getFeed(listFeedUrl, ListFeed.class);
          } catch (IOException e) {
            Log.e("Update Row", "Failed");
            e.printStackTrace();
            output = null;
            break;
          } catch (ServiceException e) {
            Log.e("Update Row", "Failed");
            e.printStackTrace();
            output = null;
            break;
          }
          ListEntry row = null;
          for (ListEntry r : listFeed.getEntries()) {
            CustomElementCollection elements = r
                .getCustomElements();
            String name = elements.getValue(specificHeading);
            if (name.compareTo(specificKey) == 0) {
              row = r;
              break;
            }

          }

          for (int i = 0; i < headings.length; i++) {
            row.getCustomElements().setValueLocal(headings[i],
                recordList[i]);
          }
          // Send the new row to the API for insertion.
          try {
            row.update();
          } catch (IOException e) {
            Log.e("Update Row", "Failed");
            e.printStackTrace();
            output = null;
            break;
          } catch (ServiceException e) {
            Log.e("Update Row", "Failed");
            e.printStackTrace();
            output = null;
            break;
          }

        }
        break;
        case DELETE_ROW: {
          String worksheetName = params[1];
          String specificHeading = params[2];
          String specificKey = params[3];

          WorksheetEntry worksheet = getWorksheet(worksheetName);
          // Define the URL to request the list feed of the worksheet.
          // Fetch the list feed of the worksheet.
          URL listFeedUrl = worksheet.getListFeedUrl();
          ListFeed listFeed = null;
          try {
            listFeed = service.getFeed(listFeedUrl, ListFeed.class);
          } catch (IOException e) {
            Log.e("Delete Row", "Failed");
            e.printStackTrace();
            output = null;
            break;
          } catch (ServiceException e) {
            Log.e("Delete Row", "Failed");
            e.printStackTrace();
            output = null;
            break;
          }
          ListEntry row = null;
          for (ListEntry r : listFeed.getEntries()) {
            CustomElementCollection elements = r
                .getCustomElements();
            String name = elements.getValue(specificHeading);
            if (name.compareTo(specificKey) == 0) {
              row = r;
              break;
            }
          }
          try {
            row.delete();
          } catch (IOException e) {
            Log.e("Delete Row", "Failed");
            e.printStackTrace();
            output = null;
            break;
          } catch (ServiceException e) {
            Log.e("Delete Row", "Failed");
            e.printStackTrace();
            output = null;
            break;
          }
        }
        break;

        }
        return output;
      } else {
        return OAuth2Helper.getErrorMessage();
      }

    }

    /**
     * Fires the appropriate event method
     */
    @Override
    protected void onPostExecute(String result) {
      String output = result;
      if (result == null) {
        output = "Error in completing request.";
      }
      else if (result.compareTo("Success")==0){
    	  output = "Success";
      }
      else{
    	  output = result.split(":")[1];
      }
      dialog.dismiss();
      if (result.split(":")[0].compareTo("ROW")==0){
    	  GotRowCount(output);
      }
      else if (result.split(":")[0].compareTo("COL")==0){
    	  GotColumnCount(output);
      }
      else if (result.split(":")[0].compareTo("CELL")==0){
    	  GotCellValue(output);
      }
      else{
    	  return;
      }
    }
  }
}
