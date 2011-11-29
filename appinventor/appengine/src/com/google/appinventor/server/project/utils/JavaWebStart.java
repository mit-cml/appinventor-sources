// Copyright 2008 Google Inc. All Rights Reserved.

package com.google.appinventor.server.project.utils;

import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.shared.rpc.ServerLayout;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

/**
 * Helper functions for supporting Java Web Start applications.
 *
 */
public final class JavaWebStart {

  private JavaWebStart() {  // COV_NF_LINE
  }  // COV_NF_LINE

  /**
   * Returns the base URL for the WebStartFileServlet.
   *
   * @param req  HTTP request for Web Start
   * @return  base URL
   */
  public static String getWebStartBaseUrl(HttpServletRequest req) {
    return req.getScheme() + "://" + req.getServerName() + ':' + req.getServerPort()
        + ServerLayout.ODE_BASEURL_NOAUTH + ServerLayout.WEBSTART_FILE_SERVLET + '/';
  }

  /**
   * Returns the project path for the given user and project IDs
   *
   * @param userProvider  user info provider
   * @param projectId  project ID
   * @return  a project path that can be added to the base url
   */
  public static String getWebStartProjectPath(UserInfoProvider userProvider, long projectId)
      throws EncryptionException {
    return Security.encryptUserAndProjectId(userProvider.getUserId(), projectId);
  }

  /**
   * Generates a JNLP file.
   *
   * @param codebase codebase for jarFiles
   * @param title  title
   * @param description description
   * @param javaVersion  the required Java version
   * @param maxHeapSize  max-heap-size value
   * @param mainClass  main class of the application
   * @param jarFiles  list of jar files belonging to the application
   * @param args list of arguments for the main class
   * @return  generated JNLP file
   */
  public static String generateJnlpFile(String codebase, String title, String description,
      String javaVersion, String maxHeapSize, String mainClass, List<String> jarFiles,
      List<String> args) {

    StringBuilder jnlp = new StringBuilder();
    jnlp.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        .append("<jnlp spec=\"1.0+\" codebase=\"").append(codebase).append("\">\n")
        .append("   <information>\n")
        .append("      <title>").append(title).append("</title>\n")
        .append("      <vendor>MIT Center for Mobile Learning</vendor>\n")
        .append("      <description>").append(description).append("</description>\n")
        .append("   </information>\n")
        .append("   <security>\n")
        .append("      <all-permissions/>\n")
        .append("   </security>\n")
        .append("   <update check=\"always\" policy=\"always\"/>\n")
        .append("   <resources>\n")
        .append("      <j2se version=\"").append(javaVersion).append("\" max-heap-size=\"")
        .append(maxHeapSize).append("\" java-vm-args=\"\"/>\n");

    for (String jarFile : jarFiles) {
      jnlp.append("      <jar href=\"").append(jarFile).append("\"/>\n");
    }
    jnlp.append("   </resources>\n")
        .append("   <application-desc main-class=\"").append(mainClass).append("\">\n");
    for (String arg : args) {
      jnlp.append("      <argument>").append(arg).append("</argument>\n");
    }
    jnlp.append("   </application-desc>\n")
        .append("</jnlp>\n");
    return jnlp.toString();
  }
}
