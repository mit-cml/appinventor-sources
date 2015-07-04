// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server;

import com.google.appengine.api.capabilities.CapabilitiesService;
import com.google.appengine.api.capabilities.CapabilitiesServiceFactory;
import com.google.appengine.api.capabilities.Capability;
import com.google.appengine.api.capabilities.CapabilityState;
import com.google.appengine.api.capabilities.CapabilityStatus;
import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * BlobUpgradeServlet -- Upgrade a user's projects from Blobstore to GCS
 *
 * This Servlet is called from the task queue manager (part of App
 * Engine). It is restricted to admin users only, which means that
 * normal people cannot directly call it.  However the task queue
 * manager operates with admin privileges, so it can always call it.
 *
 * This code verifies that memcache is available (we cannot upgrade
 * projects without it) and if so, calls storageIo.doUpgrade(), which
 * does the actual work. Because we are called from the task queue, we
 * have up to 10 minutes to run, which should be sufficient to convert
 * all the projects for any user.
 *
 */
public class BlobUpgradeServlet extends OdeServlet {
  // Logging support
  private static final Logger LOG = Logger.getLogger(BlobUpgradeServlet.class.getName());
  private static final CapabilitiesService caps = CapabilitiesServiceFactory.getCapabilitiesService();
  private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {

    String userId = req.getParameter("user");
    LOG.info("Got Request to Upgrade: " + userId);
    CapabilityState cstat = caps.getStatus(Capability.MEMCACHE);
    CapabilityStatus cstatus = cstat.getStatus();
    // Only attempt upgrade if Memcache is available
    if (cstatus == CapabilityStatus.ENABLED) {
      storageIo.doUpgrade(userId);
    }
  }
}
