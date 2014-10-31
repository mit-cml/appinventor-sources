// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2013 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.project.utils.Security;
import com.google.appinventor.server.LocalUser;
import com.google.appinventor.shared.rpc.user.User;
import com.google.appinventor.shared.rpc.RpcResult;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.storage.StorageUtil;
import com.google.common.io.ByteStreams;

import com.google.appinventor.server.project.youngandroid.YoungAndroidProjectService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to record feedback when an error occurs in the client.
 *
 * @date 20/10/2013
 * @author jis@mit.edu (Jeffrey I Schiller)
 */
public class FeedbackServlet extends OdeServlet {

  // Logging support
  private static final Logger LOG = Logger.getLogger(ReceiveBuildServlet.class.getName());

  private final OdeAuthFilter odeFilter = new OdeAuthFilter();
  private final transient StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String query = req.getQueryString();
    String notes = req.getParameter("notes");
    String foundIn = req.getParameter("foundIn");
    String faultData = req.getParameter("faultData");
    String projectId = req.getParameter("projectId");
    if (notes == null) notes = "";
    if (foundIn == null) foundIn = "";
    if (faultData == null) faultData = "";
    if (projectId == null) projectId = "-1";
    PrintWriter out = new PrintWriter(resp.getWriter());
    out.println(String.format(template, notes, foundIn, faultData, projectId));
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String query = req.getQueryString();
    String notes = req.getParameter("notes");
    String foundIn = req.getParameter("foundIn");
    String faultData = req.getParameter("faultData");
    String comments = req.getParameter("comments");
    String projectId = req.getParameter("projectId");
    String datestamp = new java.util.Date().toGMTString();
    String email = userInfoProvider.getUserEmail();

    if (notes == null) notes = "";
    if (foundIn == null) foundIn = "";
    if (faultData == null) faultData = "";
    if (projectId == null) projectId = "-1";
    if (comments == null) comments = "";
    storageIo.storeFeedback(notes, foundIn, faultData, comments, datestamp, email, projectId);
    PrintWriter out = new PrintWriter(resp.getWriter());
    out.println(thanks);
  }

  private static final String template = "<html><head><title>Ooops</title></head>\n<body>\n<h1>Ooops! We tripped on a Bug!</h1>\n<p>Please help us by telling us what you were doing at the time this happend.\nWe have already included some technical data with this report. If you do not\nsubmit this report, nothing will be reported to us. If you do submit a report,\nyour comments along with the technical data will be sent to us.</p>\n<p>\nThank you for your help in making MIT App Inventor better!\n</p>\n<form method=POST action=\"/ode/feedback\">\n<input type=hidden name=notes value=\"%1$s\">\n<input type=hidden name=foundIn value=\"%2$s\">\n<input type=hidden name=faultData value=\"%3$s\">\n<input type=hidden name=projectId value=\"%4$s\">\n<textarea cols=75 rows=5 name=comments>\n</textarea>\n<p>Technical Data to be Submitted:</p>\n<pre>\nnotes = %1$s\nfoundIn = %2$s\nfaultData = %3$s\nprojectId = %4$s\n</pre>\n<input type=submit value=\"Send Report\">\n</body>\n</html>\n";

  private static final String thanks = "<html><head><title>Ooops</title></head>\n<body>\n<h1>Thanks!</h1>\n<p>Your feedback has been recorded. Thanks again for contributing to MIT\nApp Inventor.</p>\n</body>\n</html>\n";

}
