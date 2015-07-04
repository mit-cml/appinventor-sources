// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appinventor.server.encryption.EncryptionException;
import com.google.appinventor.server.project.utils.JavaWebStart;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.server.util.JsonpConnectionUtil;
import com.google.appinventor.shared.jsonp.JsonpConnectionInfo;
import com.google.appinventor.shared.rpc.launch.LaunchService;
import com.google.appinventor.shared.rpc.user.UserInfoProvider;

import java.util.logging.Logger;

// TODO(lizlooney) - This class is no longer for launching. It is used for codeblocks,
// so it should be renamed.

/**
 * The implementation of the Launch RPC service which runs on the server.
 *
 * <p>Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 * @author lizlooney@google.com (Liz Looney)
 */
public class LaunchServiceImpl extends OdeRemoteServiceServlet implements LaunchService {
  // Logging support
  private static final Logger LOG = Logger.getLogger(LaunchServiceImpl.class.getName());

  // Storage
  private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  @Override
  public void clearJsonpConnectionInfo(String filePrefix) {
    JsonpConnectionUtil.clearJsonpConnectionInfo(storageIo, userInfoProvider.getUserId(),
        filePrefix);
  }

  @Override
  public JsonpConnectionInfo retrieveJsonpConnectionInfo(String filePrefix) {
    return JsonpConnectionUtil.retrieveJsonpConnectionInfo(storageIo, userInfoProvider.getUserId(),
        filePrefix);
  }

  @Override
  public String getWebStartProjectPath(long projectId) {
    try {
      return JavaWebStart.getWebStartProjectPath(userInfoProvider, projectId);
    } catch (EncryptionException e) {
      //TODO(sharon): what to do here?
      //throw CrashReport.createAndLogError(LOG, null, null, e);
    }
    return null;
  }
}
