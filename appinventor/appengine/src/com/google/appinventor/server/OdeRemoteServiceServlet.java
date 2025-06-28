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

import java.util.logging.Logger;

public abstract class OdeRemoteServiceServlet extends RemoteServiceServlet {
  public final static String MODULE_ALIAS = "ode";
  private static final Logger LOGGER = Logger.getLogger(OdeRemoteServiceServlet.class.getName());
  protected final UserInfoProvider userInfoProvider = LocalUser.getInstance();

  @Override
  protected SerializationPolicy doGetSerializationPolicy(final HttpServletRequest request,
    final String moduleBaseURL, final String strongName) {

    String newModuleBaseURL = moduleBaseURL;
    try {
      URL url = new URL(moduleBaseURL);
      newModuleBaseURL = String.format("%s://%s/%s/", url.getProtocol(), url.getHost(), MODULE_ALIAS);

    } catch (MalformedURLException ex) {
      LOGGER.warning("Malformed URL for moduleBaseURL: " + moduleBaseURL);
    }

    return super.doGetSerializationPolicy(request, newModuleBaseURL, strongName);
  }
}

