// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2014-2021 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.common.utils.StringUtils;

import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;

import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.storage.StoredData.ProjectNotFoundException;

import com.google.appinventor.server.storage.UserAlreadyExistsException;

import com.google.appinventor.server.tokens.Token;
import com.google.appinventor.server.tokens.TokenException;
import com.google.appinventor.server.tokens.TokenProto;

import com.google.appinventor.shared.rpc.project.youngandroid.NewYoungAndroidProjectParameters;

import com.google.appinventor.shared.rpc.user.User;

import java.io.IOException;
import java.io.PrintWriter;

import java.net.URLDecoder;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;


/**
 * REST API for managing MIT App Inventor.
 *
 * Note: A lot of the operations are not available in this code.  This
 * code was adapted from code used in the Coolthink project to
 * communicate between their proprietary learning management system
 * and App Inventor. It depends on methods added to the StorageIO
 * layer that are not implemented in the App Engine version (this
 * version).
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 */

public class RestServlet extends HttpServlet {

  private final StorageIo storageIo = StorageIoInstanceHolder.getInstance();
  private static Logger LOG;
  private final transient YoungAndroidProjectService youngAndroidProjectService = new YoungAndroidProjectService(storageIo);


  private static class RestException extends Exception {
    private int code;
    RestException(int code, String reason) {
      super(reason);
      this.code = code;
    }
    int getCode() {
      return code;
    }

  }

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    LOG = Logger.getLogger(RestServlet.class);
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    String queryString = req.getQueryString();
    HashMap<String, String> params = getQueryMap(queryString);

    String encodedToken = params.get("token");
    if (encodedToken == null) {
      LOG.error("doGet(): No Token Provided");
      fail(req, resp, 1, "No Token Provided");
      return;
    }
    TokenProto.token token = null;
    try {
      token = Token.verifyToken(encodedToken);
    } catch (TokenException e) {
      LOG.error("doGet(): Invalid Token -- " + e.getMessage());
      fail(req, resp, 2, e.getMessage());
      return;
    }

    long offset = System.currentTimeMillis() - token.getTs();
    offset /= 1000;  // Convert to seconds
    if (offset > 120) {       // Two minutes
      LOG.error("doGet(): Token Expired. Was valid until " + new Date(token.getTs()));
      fail(req, resp, 3, "Token Expired. Was valid until " +
        new Date(token.getTs()));
      return;
    }

    switch (token.getCommand()) {
    case SSOLOGIN:
    case SSOLOGIN2:
      fail(req, resp, 4, "Must use Login Servlet to SSO Login");
      return;
    case FETCHUUID:
      User user = storageIo.getUserFromEmail(token.getName());
      if (user == null) {
        fail(req, resp, -1, "Invalid User");
        return;
      }
      String retval = Token.makeUUIDReturnToken(user.getUserEmail(),
        user.getUserId());
      ok(req, resp, retval);
      return;
    default:
      fail(req, resp, -1, "Unimplemented");
    }

  }

  private void ok(HttpServletRequest req, HttpServletResponse resp, String message) throws IOException {
    resp.setContentType("application/json; charset=utf-8");
    PrintWriter out = resp.getWriter();
    out.write("{ \"ok\": \"" + message + "\"}\n");
    return;
  }

  private void fail(HttpServletRequest req, HttpServletResponse resp, int code, String message) throws IOException {
    resp.setContentType("application/json; charset=utf-8");
//    resp.setStatus(HttpServletResponse.SC_FORBIDDEN); // Don't return an HTTP level error for now
    PrintWriter out = resp.getWriter();
    out.write("{ \"error\": " + code + ", \"message\" : \"" + message + "\"}\n");
    return;
  }

  private static HashMap<String, String> getQueryMap(String query)  {
    HashMap<String, String> map = new HashMap<String, String>();
    if (query == null || query.equals("")) {
      return map;               // Empty map
    }
    String[] params = query.split("&");
    for (String param : params)  {
      String [] nvpair = param.split("=");
      if (nvpair.length <= 1) {
        map.put(nvpair[0], "");
      } else
        map.put(nvpair[0], URLDecoder.decode(nvpair[1]));
    }
    return map;
  }
}
