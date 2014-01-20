package com.google.appinventor.components.runtime;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import com.google.appinventor.components.runtime.util.YailList;

import edu.uml.cs.isense.comm.API;
import edu.uml.cs.isense.objects.RProjectField;

@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "isense.jar, httpmime.jar")
@DesignerComponent(version = YaVersion.ISENSE_PROJECT_VERSION, description = "A component that provides a high-level interface to iSENSEProject.org ", category = ComponentCategory.SOCIAL, nonVisible = true, iconName = "images/isense.png")
@SimpleObject
public class iSENSE extends AndroidNonvisibleComponent implements Component {
	private int ProjectID;
	private String UserName;
	private String Password;

	private API api;

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
	@SimpleProperty(description = "iSENSE UserName", category = PropertyCategory.BEHAVIOR)
	public String UserName() {
		return UserName;
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
	@SimpleProperty
	public void UserName(String UserName) {
		this.UserName = UserName;
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

	// Login
	@SimpleFunction(description = "Log into iSENSE")
	public void Login() {
		boolean login = api.createSession(UserName, Password);
		if (login == true) {
			LoginSucceeded();
		} else {
			LoginFailed();
		}
	}

	// upload dataset
	@SimpleFunction(description = "Upload Data Set to iSENSE")
	public void UploadDataSet(String DataSetName, YailList Fields, YailList Data)
			throws JSONException {
		ArrayList<RProjectField> projectFields = api.getProjectFields(ProjectID);
		JSONObject jData = new JSONObject();
		for (int i = 0;i < Fields.size();i++) {
			for (int j = 0; j < projectFields.size(); j++)
			{
				if (Fields.get(i+1).equals(projectFields.get(j).name))
				{
					jData.put(""+projectFields.get(j).field_id, new JSONArray().put(Data.get(i + 1)));
				}
			}
		}
		Log.i("iSENSE",jData.toString());
		int dataset = api.jsonDataUpload(ProjectID, jData, DataSetName);
		if (dataset == -1) {
			UploadDataSetFailed();
		} else {
			UploadDataSetSucceeded(dataset);
		}
	}

	// upload photos to dataset
	@SimpleFunction(description = "Upload Photos to iSENSE")
	public void UploadPhotoToDataSet(int DataSetID, String Photo) {
		String path = Photo.substring(7);
		File tmp = new File(path);
		int mediaid = api.uploadDataSetMedia(DataSetID, tmp);
		if (mediaid == -1) {
			UploadPhotoToDataSetFailed();
		} else {
			UploadPhotoToDataSetSucceeded(mediaid);
		}
	}

	// append to a data set
	@SimpleFunction(description = "Append to a preexisting Data Set")
	public void AppendDataSet(int DataSetID, YailList Fields,
			YailList Data) throws JSONException {
		ArrayList<RProjectField> projectFields = api.getProjectFields(ProjectID);
		JSONObject jData = new JSONObject();
		for (int i = 0;i < Fields.size();i++) {
			for (int j = 0; j < projectFields.size(); j++)
			{
				if (Fields.get(i+1).equals(projectFields.get(j).name))
				{
					jData.put(""+projectFields.get(j).field_id, new JSONArray().put(Data.get(i + 1)));
				}
			}
		}
		Log.i("iSENSE",jData.toString());
		api.appendDataSetData(DataSetID, jData);
		AppendDataSetSucceeded(DataSetID);
	}

	@SimpleFunction(description = "Gets the current time formatting correctly for iSENSE")
	public String GetTime() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(
				"yyyy-MM-dd'T'HH:mm:ss.SSSZ");
		return sdf.format(cal.getTime()).toString();
	}

	// @SimpleFunction(description = "logcat")
	// public void TestLogCat(String str) {
	// Log.i("iSENSE", str);
	// }

	@SimpleEvent(description = "iSENSE Upload DataSet Succeeded")
	public void UploadDataSetSucceeded(int DataSetID) {
		EventDispatcher
				.dispatchEvent(this, "UploadDataSetSucceeded", DataSetID);
	}

	@SimpleEvent(description = "iSENSE Upload DataSet Failed")
	public void UploadDataSetFailed() {
		EventDispatcher.dispatchEvent(this, "UploadDataSetFailed");
	}

	@SimpleEvent(description = "iSENSE Append DataSet Succeeded")
	public void AppendDataSetSucceeded(int DataSetID) {
		EventDispatcher
				.dispatchEvent(this, "AppendDataSetSucceeded", DataSetID);
	}

	@SimpleEvent(description = "iSENSE Append DataSet Failed")
	public void AppendDataSetFailed() {
		EventDispatcher.dispatchEvent(this, "AppendDataSetFailed");
	}

	@SimpleEvent(description = "iSENSE Upload Photo To DataSet Succeeded")
	public void UploadPhotoToDataSetSucceeded(int PhotoID) {
		EventDispatcher.dispatchEvent(this, "UploadPhotoToDataSetSucceeded",
				PhotoID);
	}

	@SimpleEvent(description = "iSENSE Upload Photo To DataSet Failed")
	public void UploadPhotoToDataSetFailed() {
		EventDispatcher.dispatchEvent(this, "UploadPhotoToDataSetFailed");
	}

	@SimpleEvent(description = "iSENSE Login Succeeded")
	public void LoginSucceeded() {
		EventDispatcher.dispatchEvent(this, "LoginSucceeded");
	}

	@SimpleEvent(description = "iSENSE Login Failed")
	public void LoginFailed() {
		EventDispatcher.dispatchEvent(this, "LoginFailed");
	}
}
