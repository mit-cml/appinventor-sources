package com.google.appinventor.client.utils;

import com.google.appinventor.client.Ode;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.user.User;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.xhr.client.ReadyStateChangeHandler;
import com.google.gwt.xhr.client.XMLHttpRequest;

import java.util.Objects;

public class PostUtil {

    public static void addAppToGallery(User user, long projectId, String projectName) {
        new PostUtil().postAddNewUserAndPublishApp(user, "ai2", projectId, projectName);
    }

    public void postAddNewUserAndPublishApp(final User user, String appInventorInstance, final long projectId, final String projectName) {
        XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
            @Override
            public void onReadyStateChange(XMLHttpRequest xhr) {
                if (xhr.getReadyState() == XMLHttpRequest.DONE) {
                    new PostUtil().postAddAppToGallery(user.getUserEmail(), projectId, projectName);
                }
            }
        });
        xhr.open("POST", "http://localhost:8090/api/user/create");
        xhr.setRequestHeader("Content-Type", "application/json");
        JSONObject json = new JSONObject();
        try {
            json.put("authorId", new JSONString(user.getUserEmail()));
            json.put("name", new JSONString(user.getUserName()));
            json.put("username", new JSONString(user.getUserName() + "_" + user.getUserId()));
            json.put("appInventorInstance", new JSONString(appInventorInstance));
            xhr.send(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void postAddAppToGallery(String email, long projectId, String projectName) {
        String aiaUrl = GWT.getModuleBaseURL() + ServerLayout.DOWNLOAD_SERVLET_BASE + ServerLayout.DOWNLOAD_PROJECT_SOURCE + "/" + Objects.toString(projectId);
        String authToken = Cookies.getCookie("galleryToken");
        addAppToGalleryPostHelper(aiaUrl, projectName, email, Objects.toString(projectId), authToken);
    }

    public static void updateProjectPublishedOrUnpublished(final long projectId) {
        XMLHttpRequest xhr = XMLHttpRequest.create();
        xhr.setOnReadyStateChange(new ReadyStateChangeHandler() {
            @Override
            public void onReadyStateChange(XMLHttpRequest xhr) {
                if (xhr.getReadyState() == XMLHttpRequest.DONE) {
                    int galleryId = Integer.parseInt(xhr.getResponseText());

                    if (galleryId > 0) {
                        Ode.getInstance().getProjectManager().publishProject(projectId, galleryId);
                    } else {
                        Ode.getInstance().getProjectManager().UnpublishProject(projectId);
                    }
                }
            }
        });
        xhr.open("GET", "http://localhost:8090/api/project/gallery_id?projectId=" + Objects.toString(projectId) + "&appInventorInstance=ai2");
        xhr.send();
    }

    // Called from addAppToGalleryPostHelper
    // Need overloaded method with String parameter because JSNI disallows passing long type as Java parameter
    public static void updateProjectPublishedOrUnpublished(final String projectId) {
        updateProjectPublishedOrUnpublished(Long.parseLong(projectId));
    }

    public static native void addAppToGalleryPostHelper(String aiaUrl, String title, String authorId, String projectId, String authToken)/*-{
    var aiaRequest = new XMLHttpRequest();
    aiaRequest.responseType = "blob";
    aiaRequest.onreadystatechange = function() {
        if (aiaRequest.readyState === 4) {
            var formData = new FormData();

            formData.append("title", title);
            formData.append("authorId", authorId);
            formData.append("projectId", projectId);
            formData.append("appInventorInstance", "ai2");
            formData.append("aia", aiaRequest.response);
            formData.append("token", authToken);

            var request = new XMLHttpRequest();
            request.responseType = "json";
            request.onreadystatechange = function() {
                if (request.readyState === 4) {
                    if (request.response.project && request.response.project.id) {
                        window.open("http://localhost:3000/#/project/" + request.response.project.id, "_blank");
                        @com.google.appinventor.client.utils.PostUtil::updateProjectPublishedOrUnpublished(Ljava/lang/String;)(projectId);
                    }
                }
            }
            request.open("POST", "http://localhost:8090/api/project/update_or_create");
            request.send(formData);
        }
    }
    aiaRequest.open("GET", aiaUrl);
    aiaRequest.send();
    }-*/;
}
