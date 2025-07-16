// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

/**
 * Configuration of the URL namespace on the ODE server.
 *
 * Note that this must match the paths defined in the war/WEB-INF/web.xml file.
 *
 */
public class ServerLayout {
  /**
   * Absolute path prefix that defines where the ODE services and servlets live.
   */
  public static final String ODE_BASEURL = "/ode/";

  /**
   * Absolute path prefix that defines where the ODE servlet lives that does not
   * require Google Account authentication. Used by the blocks editor.
   */
  public static final String ODE_BASEURL_NOAUTH = "/ode2/";

  /*
   * Last component of the logout URL path
   */
  public static final String ODE_LOGOUT_URL_LAST = "_logout";

  /**
   * Absolute path of the {@link com.google.appinventor.server.LogoutServlet}.
   */
  public static final String ODE_LOGOUT_URL = ODE_BASEURL + ODE_LOGOUT_URL_LAST;

  /**
   * Relative path of the
   * {@link com.google.appinventor.shared.rpc.component.ComponentService} within
   * the ODE GWT module.
   */
  public static final String COMPONENT_SERVICE = "components";

  /**
   * Relative path of the
   * {@link com.google.appinventor.shared.rpc.project.ProjectService} within
   * the ODE GWT module.
   */
  public static final String PROJECT_SERVICE = "projects";

  /**
   * Relative path of the
   * {@link com.google.appinventor.shared.rpc.user.UserInfoService} within the
   * ODE GWT module.
   */
  public static final String USER_INFO_SERVICE = "userinfo";

  /**
   * Relative path of the Admin Info Service within ODE
   */

  public static final String ADMIN_INFO_SERVICE = "admininfo";

  /**
   * Relative path of the
   * {@link com.google.appinventor.server.TosServlet}
   * within the ODE GWT module.
   */
  public static final String ACCEPT_TOS_SERVLET = "accept_tos";

  /**
   * Relative base path of the
   * {@link com.google.appinventor.server.DownloadServlet} within the ODE GWT
   * module.
   */
  public static final String DOWNLOAD_SERVLET_BASE = "download/";

  /**
   * Relative path within {@link com.google.appinventor.server.DownloadServlet}
   * for downloading a project's output
   */
  public static final String DOWNLOAD_PROJECT_OUTPUT = "project-output";

  /**
   * Relative path within {@link com.google.appinventor.server.DownloadServlet}
   * for downloading a project's sources
   */
  public static final String DOWNLOAD_PROJECT_SOURCE = "project-source";

  /**
   * Relative path within {@link com.google.appinventor.server.DownloadServlet}
   * for downloading a project's sources for local device caching.
   */
  public static final String DOWNLOAD_PROJECT_CACHED = "project-cached";

  /**
   * Relative path within {@link com.google.appinventor.server.DownloadServlet}
   * for downloading selected of a user's projects' sources.
   */
  public static final String DOWNLOAD_SELECTED_PROJECTS_SOURCE = "selected-projects-source";

  /**
   * Relative path within {@link com.google.appinventor.server.DownloadServlet}
   * for downloading all of a user's projects' sources.
   */
  public static final String DOWNLOAD_ALL_PROJECTS_SOURCE = "all-projects-source";

  /**
   * Relative path within {@link com.google.appinventor.server.DownloadServlet}
   * for downloading a project's sources for a user other than the logged-in
   * user (admin only)
   */
  public static final String DOWNLOAD_USER_PROJECT_SOURCE = "user-project-source";

  /**
   * Relative path within {@link com.google.appinventor.server.DownloadServlet}
   * for downloading a file
   */
  public static final String DOWNLOAD_FILE = "file";

  /**
   * Relative path within {@link com.google.appinventor.server.DownloadServlet}
   * for downloading a user file
   */
  public static final String DOWNLOAD_USERFILE = "userfile";

  /**
   * Relative path of the {@link com.google.appinventor.server.UploadServlet}
   * within the ODE GWT module.
   */
  public static final String UPLOAD_SERVLET = "upload";

  /**
   * Relative path within {@link com.google.appinventor.server.UploadServlet}
   * for uploading a project
   */
  public static final String UPLOAD_PROJECT = "project";

  /**
   * Relative path within {@link com.google.appinventor.server.UploadServlet}
   * for uploading a component
   */
  public static final String UPLOAD_COMPONENT = "component";

  /**
   * Relative path within {@link com.google.appinventor.server.UploadServlet}
   * for uploading a file
   */
  public static final String UPLOAD_FILE = "file";

  /**
   * Relative path within {@link com.google.appinventor.server.UploadServlet}
   * for uploading a user file
   */
  public static final String UPLOAD_USERFILE = "userfile";

  /**
   * Relative path of the {@link com.google.appinventor.server.components.FirebaseAuthServiceImpl}
   * within the ODE GWT module.
   */
  public static final String FIREBASE_AUTH_SERVICE = "firebase_auth";

  /**
   * Relative path of the form through which the user can see and accept
   * the Young Android terms of service.
   */
  public static final String YA_TOS_FORM = "Ya_tos_form.html";

  /**
   * Relative path of the {@link com.google.appinventor.server.ReceiveBuildServlet} within
   * {@link #ODE_BASEURL_NOAUTH}
   */
  public static final String RECEIVE_BUILD_SERVLET = "receivebuild";

  /**
   * Name of the form element to upload files.
   */
  public static final String UPLOAD_FILE_FORM_ELEMENT = "uploadFile";

  /**
   * Name of the form element to upload project archives.
   */
  public static final String UPLOAD_PROJECT_ARCHIVE_FORM_ELEMENT = "uploadProjectArchive";

  /**
   * Name of the form element to upload component archives.
   */
  public static final String UPLOAD_COMPONENT_ARCHIVE_FORM_ELEMENT = "uploadComponentArchive";

  /**
   * Name of the form element to upload user files.
   */
  public static final String UPLOAD_USERFILE_FORM_ELEMENT = "uploadUserFile";

  /**
   * Relative path of the
   * {@link com.google.appinventor.shared.rpc.cloudDB.TokenAuthService} within the
   * ODE GWT module.
   */
  public static final String TOKEN_AUTH_SERVICE = "tokenauth";

  public static String genRelativeDownloadPath(long projectId, String target) {
    return DOWNLOAD_SERVLET_BASE + DOWNLOAD_PROJECT_OUTPUT + "/" + projectId + "/" + target;
  }

  public static String genFullDownloadPath(long projectId, String target) {
    return ODE_BASEURL + genRelativeDownloadPath(projectId, target);
  }

  public static String getModuleBaseURL() {
    return Window.Location.getProtocol() + "//" + Window.Location.getHost() +
      "/" + GWT.getModuleName() + "/";
  }

}
