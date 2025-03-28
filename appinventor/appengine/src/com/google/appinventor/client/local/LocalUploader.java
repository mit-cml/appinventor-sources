// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.client.local;

import com.google.appinventor.client.Ode;
import com.google.appinventor.client.utils.Promise;
import com.google.appinventor.client.utils.Uploader;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.UploadResponse;
import com.google.appinventor.shared.rpc.UploadResponse.Status;
import com.google.appinventor.shared.rpc.project.UserProject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FileUpload;

import java.util.HashMap;
import java.util.Map;

import static com.google.appinventor.client.Ode.MESSAGES;

public class LocalUploader extends Uploader {
  interface UploadHandler {
    void process(FileUpload upload, String[] urlParts, AsyncCallback<UploadResponse> callback);
  }

  private static final Map<String, UploadHandler> UPLOAD_HANDLERS = new HashMap<>();

  static {
    UPLOAD_HANDLERS.put(ServerLayout.UPLOAD_PROJECT, LocalUploader::handleProjectUpload);
  }

  @Override
  public void upload(FileUpload upload, String uploadUrl, AsyncCallback<UploadResponse> callback) {
    String[] parts = uploadUrl.replace(ServerLayout.getModuleBaseURL(), "").split("/");
    if (parts.length < 3) {
      callback.onFailure(new IllegalArgumentException("Invalid upload URL: " + uploadUrl));
      return;
    }
    UploadHandler handler = UPLOAD_HANDLERS.get(parts[1]);
    if (handler != null) {
      handler.process(upload, parts, callback);
    } else {
      callback.onFailure(new UnsupportedOperationException("Unsupported upload url: " + uploadUrl));
    }
  }

  private static void handleProjectUpload(FileUpload upload, String[] urlParts,
      AsyncCallback<UploadResponse> callback) {
    String projectName = urlParts[2];
    getFileBase64(upload.getElement())
        .then(zipBase64 -> Promise.<UserProject>call(MESSAGES.projectUploadError(),
            c -> Ode.getInstance().getProjectService()
                .newProjectFromExternalTemplate(projectName, zipBase64, c)))
        .then(project -> {
          UploadResponse response = new UploadResponse(Status.SUCCESS, 0, project.toString());
          callback.onSuccess(response);
          return Promise.resolve(response);
        })
        .error(error -> {
          callback.onFailure(new Exception("Failed to upload file: " + error));
          return Promise.reject(error);
        });
  }

  private static native Promise<String> getFileBase64(Element element) /*-{
    function _arrayBufferToBase64(buffer) {
      var binary = '';
      var bytes = new Uint8Array(buffer);
      var len = bytes.byteLength;
      for (var i = 0; i < len; i++) {
        binary += String.fromCharCode(bytes[i]);
      }
      return $wnd.btoa(binary);
    }
    return new Promise(function(resolve, reject) {
      var reader = new FileReader();
      reader.onload = function(event) {
        resolve(_arrayBufferToBase64(event.target.result));
      };
      reader.onerror = function(event) {
        reject(new Error("Failed to read file: " + event.target.error));
      };
      reader.readAsArrayBuffer(element.files[0]);
    });
  }-*/;
}
