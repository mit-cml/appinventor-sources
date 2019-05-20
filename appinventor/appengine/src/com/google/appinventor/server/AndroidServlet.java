// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.util.CacheHeaders;
import com.google.appinventor.server.util.CacheHeadersImpl;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.project.youngandroid.YoungAndroidProjectNode;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet for creating a page for downloading project output on an Android
 * device.
 *
 */
public class AndroidServlet extends OdeServlet {

  // Object used to safely set cache headers in responses
  private static final CacheHeaders CACHE_HEADERS = new CacheHeadersImpl();

  // Content type for response header (to avoid security vulnerabilities)
  private static final String CONTENT_TYPE = "text/html; charset=utf-8";

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    setDefaultHeader(resp);

    PrintWriter out = resp.getWriter();
    String logo = "/images/logo_ya.png";
    StringBuilder htmlString = new StringBuilder();
    htmlString
        .append("<html>\n")
        .append("<head>\n")
        .append("<title>App Inventor for Android Downloads</title>\n")
        .append("<link type=\"text/css\" rel=\"stylesheet\" href=\"/Ya.css\">\n")
        .append("</head>\n")
        .append("<body>\n")
        .append("<table width=\"100%\"><tr><td height=\"16\" align=\"left\">\n")
        .append("<img src=\"\n")
        .append(logo)
        .append("\"></td>\n")
        .append("<td class=\"ode-Android-banner\">Downloads</td>\n")
        .append("</tr></table><hr>\n")
        .append("<table width=\"100%\"><tr><td height=\"16\" align=\"left\">\n")
        .append("<div class=\"ode-Android-header\">\n")
        .append("<b>\n")
        .append(userInfoProvider.getUserEmail())
        .append("</b> | <a href='\n")
        .append(ServerLayout.ODE_LOGOUT_URL)
        .append("'>Sign Out</a>\n")
        .append("</div>\n")
        .append("</td></tr></table>\n")
        .append("<table width=\"100%\"><tr><td height=\"16\" align=\"left\">\n")
        .append("<div class=\"ode-Android-caption\">\n")
        .append("Projects\n")
        .append("</div>\n")
        .append("</td></tr></table>\n");
    out.println(htmlString.toString());

    out.println("<table class=\"ode-Android-body\">");
    String userId = userInfoProvider.getUserId();
    List<Long> projectIds = storageIo.getProjects(userId);
    for (long projectId : projectIds) {
      String projectType = storageIo.getProjectType(userId, projectId);
      String projectName = storageIo.getProjectName(userId, projectId);
      if (projectType.equals(YoungAndroidProjectNode.YOUNG_ANDROID_PROJECT_TYPE)) {
        String target = YoungAndroidProjectNode.YOUNG_ANDROID_TARGET_ANDROID;
        // TODO(lizlooney) - we need a 16x16 image for young android.
        String icon = "/images/logo_16.png";
        if (containsAndroidTarget(storageIo.getProjectOutputFiles(userId, projectId), target)) {
          out.println(makeAndroidTargetRow(projectId, projectName, target, icon));
        }
      }
    }
    out.println("</table>");

    out.println("<table width=\"100%\"><tr><td height=\"12\" align=\"left\">" +
        "<div class=\"ode-Android-footer\">" +
        "</div>" +
        "</td></tr></table>" +
        "</body>" +
        "</html>");
  }

  /*
   * Indicates whether the project contains any project output files for Android.
   */
  private static boolean containsAndroidTarget(List<String> outputFiles, String target) {
    for (String outputFile : outputFiles) {
      if (outputFile.startsWith("build/" + target + '/')) {
        return true;
      }
    }
    return false;
  }

  private static String makeAndroidTargetRow(long projectId, String projectName,
      String target, String image) {
    String projectUrl = ServerLayout.genFullDownloadPath(projectId, target);
    return "<tr><td><img src=\"" + image + "\" width=16 height=16> <a href=\"" + projectUrl + "\">" +
        projectName + "</a></td></tr>";
  }

  /*
   * Set a default http header to avoid security vulnerabilities.
   */
  private static void setDefaultHeader(HttpServletResponse resp) {
    CACHE_HEADERS.setNotCacheable(resp);
    resp.setContentType(CONTENT_TYPE);
  }
}
