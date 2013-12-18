package com.google.appinventor.components.runtime;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
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

@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "isense.jar")
@DesignerComponent(version = YaVersion.ISENSE_PROJECT_VERSION, description = "A component that provides a high-level interface to iSENSEProject.org ", category = ComponentCategory.ISENSE, nonVisible = true)
@SimpleObject
public class iSENSE extends AndroidNonvisibleComponent implements Component {
	private int ProjectID;

	private API api;

	// @DesignerProperty(editorType =
	// PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "AppInventor")
	// @SimpleProperty
	// public void UserName(String user) {
	// user_name = user;
	// }
	//
	// @SimpleProperty(description = "TEST1", category =
	// PropertyCategory.BEHAVIOR, userVisible = false)
	// public String UserName() {
	// return user_name;
	// }

	public iSENSE(ComponentContainer container) {
		super(container.$form());
		Log.i("iSENSE", "Starting? " + container.toString());
		api = API.getInstance(container.$context());
		// api.useDev(true);
		ProjectID();
	}

	// ProjectID
	@SimpleProperty(description = "iSENSE Project ID", category = PropertyCategory.BEHAVIOR, userVisible = false)
	public int ProjectID() {
		return ProjectID;
	}

	@DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
	@SimpleProperty
	public void ProjectID(int ProjectID) {
		this.ProjectID = ProjectID;
	}

	// Login
	@SimpleFunction(description = "Log into iSENSE")
	public boolean Login(String UserName, String Password) {
		return api.createSession(UserName, Password);
	}

	// Log out
	@SimpleFunction(description = "Log out of iSENSE")
	public void LogOut() {
		api.deleteSession();
	}

	// upload
	@SimpleFunction(description = "Upload Data Set to iSENSE")
	public int UploadDataSet(String DataSetName, YailList data)
			throws JSONException {
		JSONObject jdata = new JSONObject();
		for (int i = 0; i < data.size(); i++) {
			jdata.put(Integer.toString(i), new JSONArray().put(data.get(i + 1)));
		}
		Log.i("iSENSE", "data: " + jdata.toString());
		// jdata.put("0", new JSONArray().put("6"));
		// jdata.put("1", new JSONArray().put("3"));
		// Log.i("iSENSE", "jodata: " + jdata.toString());
		return api.uploadDataSet(ProjectID, jdata, DataSetName);
	}
}
