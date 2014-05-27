package com.google.appinventor.components.runtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import kawa.standard.Scheme;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

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
import com.google.appinventor.components.runtime.util.AsyncCallbackPair;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.YailList;

import edu.uml.cs.isense.api.API;
import edu.uml.cs.isense.objects.RDataSet;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.objects.RProjectField;

@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "isense.jar, httpmime.jar")
@DesignerComponent(version = YaVersion.ISENSE_PROJECT_VERSION,
                   description = "A component that provides a high-level interface to iSENSEProject.org",
                   category = ComponentCategory.SOCIAL,
                   nonVisible = true,
                   iconName = "images/isense.png")
@SimpleObject
public class iSENSE extends AndroidNonvisibleComponent implements Component {
  private int ProjectID;
  private String Email;
  private String Password;
  private String ContributorKey;
  private String YourName;
  private API api;
  private int LoginType = -1;
  private Handler handler;

  private boolean InProgress;

  public iSENSE(ComponentContainer container) {
    super(container.$form());
    Log.i("iSENSE", "Starting? " + container.toString());
    LoginType(Component.iSENSE_LOGIN_TYPE_EMAIL + "");
    api = API.getInstance();
    handler = new Handler();
    // api.useDev(true);
  }

  // Block Properties
  // ProjectID
  @SimpleProperty(description = "iSENSE Project ID", category = PropertyCategory.BEHAVIOR)
  public int ProjectID() {
    return ProjectID;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void ProjectID(int ProjectID) {
    this.ProjectID = ProjectID;
  }

  // UserName
  @SimpleProperty(description = "iSENSE Email", category = PropertyCategory.BEHAVIOR)
  public String Email() {
    return Email;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void Email(String Email) {
    this.Email = Email;
  }

  // Password
  @SimpleProperty(description = "iSENSE Password", category = PropertyCategory.BEHAVIOR)
  public String Password() {
    return Password;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void Password(String Password) {
    this.Password = Password;
  }

  // Contributor Key
  @SimpleProperty(description = "iSENSE Contributor Key", category = PropertyCategory.BEHAVIOR)
  public String ContributorKey() {
    return ContributorKey;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void ContributorKey(String ContributorKey) {
    this.ContributorKey = ContributorKey;
  }

  // Name
  @SimpleProperty(description = "iSENSE Your Name", category = PropertyCategory.BEHAVIOR)
  public String YourName() {
    return YourName;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void YourName(String YourName) {
    this.YourName = YourName;
  }

  // Login Type
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
                  description = "This selects how you will login to iSENSEProject.org.  Either an email or a contributor key")
  public int LoginType() {
    return LoginType;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ISENSE_LOGIN_TYPE,
                    defaultValue = Component.iSENSE_LOGIN_TYPE_EMAIL + "")
  @SimpleProperty
  public void LoginType(String LoginType) {
    this.LoginType = Integer.parseInt(LoginType);
  }

  // Block Functions
  @SimpleFunction(description = "Upload Data Set to iSENSE")
  public void UploadDataSet(final String DataSetName, final YailList Fields, final YailList Data) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        // Get fields from project
        ArrayList<RProjectField> projectFields = api.getProjectFields(ProjectID);
        JSONObject jData = new JSONObject();
        for (int i = 0; i < Fields.size(); i++) {
          for (int j = 0; j < projectFields.size(); j++) {
            if (Fields.get(i + 1).equals(projectFields.get(j).name)) {
              try {
                String sdata = SanitizeString(Data.get(i + 1).toString());
                jData.put("" + projectFields.get(j).field_id, new JSONArray().put(sdata));
              } catch (JSONException e) {
                UploadDataSetResult(-1);
                e.printStackTrace();
                return;
              }
            }
          }
        }
        int dataset = -1;
        // login with email
        if (LoginType == iSENSE_LOGIN_TYPE_EMAIL) {
          RPerson user = api.createSession(Email, Password);
          if (user == null) {
            UploadDataSetResult(-1);
            return;
          }
          dataset = api.uploadDataSet(ProjectID, jData, DataSetName);
          // login with contribution key
        } else if (LoginType == iSENSE_LOGIN_TYPE_KEY) {
          dataset = api.uploadDataSet(ProjectID, DataSetName, jData, ContributorKey, YourName);
        }
        Log.i("iSENSE", "JSON Upload: " + jData.toString());
        Log.i("iSENSE", "Dataset ID: " + dataset);
        UploadDataSetResult(dataset);
      }
    });
  }

  private void UploadDataSetResult(int dataset) {
    AsyncCallbackPair<Integer> myCallback = new AsyncCallbackPair<Integer>() {
      public void onSuccess(final Integer result) {
        handler.post(new Runnable() {
          public void run() {
            UploadDataSetSucceeded(result);
          }
        });
      }

      public void onFailure(final String message) {
        handler.post(new Runnable() {
          public void run() {
            UploadDataSetFailed();
          }
        });
      }
    };
    if (dataset == -1) {
      myCallback.onFailure("");
    } else {
      myCallback.onSuccess(dataset);
    }
  }

  @SimpleFunction(description = "Get the Data Sets for the current project")
  public YailList GetDataSetsByField(final String Field) {
    String FieldID = null;
    ArrayList<RDataSet> project_data = api.getDataSets(ProjectID);
    ArrayList<RDataSet> rdata = new ArrayList<RDataSet>();
    ArrayList<RProjectField> projectFields = api.getProjectFields(ProjectID);
    ArrayList<String> fdata = new ArrayList<String>();
    for (RProjectField f : projectFields) {
      if (f.name.equals(Field)) {
        FieldID = f.field_id + "";
      }
    }
    for (RDataSet r : project_data) {
      rdata.add(api.getDataSet(r.ds_id));
    }
    for (RDataSet r : rdata) {
      try {
        Log.i("iSENSE", "fdata:" + r.data.getString(FieldID));
        JSONArray jadata = new JSONArray();
        jadata = r.data.getJSONArray(FieldID);
        for (int i = 0; i < jadata.length(); i++) {
          fdata.add(jadata.getString(i));
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return YailList.makeList(fdata);
  }

  @SimpleFunction(description = "Gets the current time. It is formated correctly for iSENSE")
  public String GetTime() {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return sdf.format(cal.getTime()).toString();
  }

  // Block Events
  @SimpleEvent(description = "iSENSE Upload DataSet Succeeded")
  public void UploadDataSetSucceeded(int DataSetID) {
    EventDispatcher.dispatchEvent(this, "UploadDataSetSucceeded", DataSetID);
  }

  @SimpleEvent(description = "iSENSE Upload DataSet Failed")
  public void UploadDataSetFailed() {
    EventDispatcher.dispatchEvent(this, "UploadDataSetFailed");
  }

  // @SimpleEvent(description = "iSENSE Append DataSet Succeeded")
  // public void AppendDataSetSucceeded(int DataSetID) {
  // EventDispatcher.dispatchEvent(this, "AppendDataSetSucceeded", DataSetID);
  // }
  //
  // @SimpleEvent(description = "iSENSE Append DataSet Failed")
  // public void AppendDataSetFailed() {
  // EventDispatcher.dispatchEvent(this, "AppendDataSetFailed");
  // }

  @SimpleEvent(description = "iSENSE Upload Photo To DataSet Succeeded")
  public void UploadPhotoToDataSetSucceeded(int PhotoID) {
    EventDispatcher.dispatchEvent(this, "UploadPhotoToDataSetSucceeded", PhotoID);
  }

  @SimpleEvent(description = "iSENSE Upload Photo To DataSet Failed")
  public void UploadPhotoToDataSetFailed() {
    EventDispatcher.dispatchEvent(this, "UploadPhotoToDataSetFailed");
  }

  // Sanitize a string
  // fraction gets converted to a number
  // string gets returned
  private String SanitizeString(final String str) {
    Log.i("test", "test:" + str);
    Scheme scheme = new Scheme();
    String scheme_str = "(if (number? " + str + ") (exact->inexact " + str + ") \"" + str + "\")";
    try {
      return scheme.eval(scheme_str).toString();
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return null;
  }

}
