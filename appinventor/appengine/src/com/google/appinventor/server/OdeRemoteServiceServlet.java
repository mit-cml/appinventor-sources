// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Class supporting ODE GWT RPC, which ODE RPC
 * implementations should subclass instead of
 * {@link javax.servlet.http.HttpServlet}
 *
 * <p>Currently, this is only a placeholder. Once upon a time it did logging
 * and session verification, but that has been removed during the  transition
 * to App Engine.
 * </ul>
 *
 * <p>If subclasses provided a
 * {@link com.google.appinventor.shared.rpc.user.UserInfoProvider} to the
 * constructor, this class will log the appropriate userId.
 *
 * <p>Here is an example of how the methods in this class might be called
 * from a concrete subclass serving a request:
 * <pre>
 * public List<ProjectNode> getChildren(long projectId, String parentFileId) {
 *   // Get the protocol buffer for logging information and specify the service
 *   // we are providing
 *
 *   // Generate the RPC return value and, before returning it, log its size
 *   List<ProjectNode> projectNodes =
 *     getProjectRpcImpl(projectId).getChildren(projectId, parentFileId);
 *   return projectNodes;
 * }
 *
 * <p>The class {@link OdeServlet} serves an analogous purpose for non-GWT
 * servlets.
 *
 */

import com.google.gwt.user.server.rpc.SerializationPolicy;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class OdeRemoteServiceServlet extends RemoteServiceServlet {
  public final static String MODULE_ALIAS = "ode";
  protected final UserInfoProvider userInfoProvider = LocalUser.getInstance();

  @Override
  protected SerializationPolicy doGetSerializationPolicy(final HttpServletRequest request,
    final String moduleBaseURL, final String strongName) {

    // true client side relative location is the app name
    String newModuleBaseURL = moduleBaseURL;
    try {
      URL url = new URL(moduleBaseURL);

      StringBuilder builder = new StringBuilder();
      builder.append(url.getProtocol());
      builder.append("://");
      builder.append(url.getHost());
      builder.append("/");
      builder.append(MODULE_ALIAS);
      builder.append("/");
      newModuleBaseURL = builder.toString();

    } catch (MalformedURLException ex) {
      // we have no affect
    }

    return super.doGetSerializationPolicy(request, newModuleBaseURL, strongName);
  }

}
