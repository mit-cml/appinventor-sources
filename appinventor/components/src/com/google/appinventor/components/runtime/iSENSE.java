package com.google.appinventor.components.runtime;

import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.ErrorMessages;

import edu.uml.cs.isense.api.API;


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
  private int LoginType;
  
  private boolean InProgress;

  public iSENSE(ComponentContainer container) {
    super(container.$form());
    Log.i("iSENSE", "Starting? " + container.toString());
    api = API.getInstance();
    // api.useDev(true);
  }

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
  
  //Contributor Key
  @SimpleProperty(description = "iSENSE Contributor Key", category = PropertyCategory.BEHAVIOR)
  public String ContributorKey() {
    return ContributorKey;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void ContributorKey(String ContributorKey) {
    this.ContributorKey = ContributorKey;
  }

  //Name
  @SimpleProperty(description = "iSENSE Your Name", category = PropertyCategory.BEHAVIOR)
  public String YourName() {
    return YourName;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void YourName(String YourName) {
    this.YourName = YourName;
  }

  /**
   * Returns a number that selects how you will login to isenseproject.org
   * The choices are: 1 = email, 2 = key
   *
   * @return  one of {@link Component#iSENSE_LOGIN_TYPE_EMAIL},
   *          {@link Component#iSENSE_LOGIN_TYPE_KEY}
   */
  @SimpleProperty(
      category = PropertyCategory.APPEARANCE,
      description = "This selects how you will login to iSENSEProject.org.  Either an email or a contributor key")
  public int LoginType() {
    return LoginType;
  }
  
  /**
   * Specifies the sensitivity of the accelerometer
   * and checks that the argument is a legal value.
   *
   * @param sensitivity one of {@link Component#iSENSE_LOGIN_TYPE_EMAIL},
   *          {@link Component#iSENSE_LOGIN_TYPE_KEY}
   *
   */
  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ISENSE_LOGIN_TYPE,
      defaultValue = Component.iSENSE_LOGIN_TYPE_EMAIL + "")
  @SimpleProperty
  public void LoginType(int LoginType) {
    this.LoginType = LoginType;
  }
/*  // upload dataset
  @SimpleFunction(description = "Upload Data Set to iSENSE")
  public void UploadDataSet(final String DataSetName, final YailList Fields, final YailList Data) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        // Login, if failed trigger event
        boolean login = api.createSession(Email, Password);
        if (login == false) {
          LoginFailed();
          return;
        }
        // Get fields from project
        ArrayList<RProjectField> projectFields = api.getProjectFields(ProjectID);
        //
        JSONObject jData = new JSONObject();
        for (int i = 0; i < Fields.size(); i++) {
          for (int j = 0; j < projectFields.size(); j++) {
            if (Fields.get(i + 1).equals(projectFields.get(j).name)) {
              try {
                jData.put("" + projectFields.get(j).field_id, new JSONArray().put(Data.get(i + 1)));
              } catch (JSONException e) {
                UploadDataSetFailed();
                e.printStackTrace();
                return;
              }
            }
          }
        }
        Log.i("iSENSE", "JSON Upload: " + jData.toString());
        int dataset = api.jsonDataUpload(ProjectID, jData, DataSetName);
        if (dataset == -1) {
          UploadDataSetFailed();
        } else {
          UploadDataSetSucceeded(dataset);
        }
      }
    });
  }

  // upload photos to Data Set
  @SimpleFunction(description = "Upload Photos to iSENSE")
  public void UploadPhotoToDataSet(final int DataSetID, final String Photo) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        // Login, if failed trigger event
        boolean login = api.createSession(Email, Password);
        if (login == false) {
          LoginFailed();
          return;
        }
        // upload photos
        String path = Photo.substring(7);
        File tmp = new File(path);
        if (tmp.exists() == false) {
          UploadPhotoToDataSetFailed();
          return;
        }
        int mediaid = api.uploadDataSetMedia(DataSetID, tmp);
        if (mediaid == -1) {
          UploadPhotoToDataSetFailed();
        } else {
          UploadPhotoToDataSetSucceeded(mediaid);
        }
      }
    });
  }

  // // append to a data set
  // @SimpleFunction(description = "Append to a preexisting Data Set")
  // public void AppendDataSet(int DataSetID, YailList Fields, YailList Data) throws JSONException {
  // ArrayList<RProjectField> projectFields = api.getProjectFields(ProjectID);
  // JSONObject jData = new JSONObject();
  // for (int i = 0; i < Fields.size(); i++) {
  // for (int j = 0; j < projectFields.size(); j++) {
  // if (Fields.get(i + 1).equals(projectFields.get(j).name)) {
  // jData.put("" + projectFields.get(j).field_id, new JSONArray().put(Data.get(i + 1)));
  // }
  // }
  // }
  // Log.i("iSENSE", jData.toString());
  // api.appendDataSetData(DataSetID, jData);
  // AppendDataSetSucceeded(DataSetID);
  // }

  @SimpleFunction(description = "Gets the current time. It is formated correctly for iSENSE")
  public String GetTime() {
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return sdf.format(cal.getTime()).toString();
  }

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

  // @SimpleEvent(description = "iSENSE Login Succeeded")
  // public void LoginSucceeded() {
  // EventDispatcher.dispatchEvent(this, "LoginSucceeded");
  // }

  @SimpleEvent(description = "iSENSE Login Failed")
  public void LoginFailed() {
    EventDispatcher.dispatchEvent(this, "LoginFailed");
  }*/
}
