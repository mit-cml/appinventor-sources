// Copyright 2010 Google Inc. All Rights Reserved.

package com.google.appinventor.server;

import com.google.appinventor.server.flags.Flag;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import com.google.appinventor.shared.rpc.GetMotdService;
import com.google.appinventor.shared.rpc.Motd;

import java.util.logging.Logger;



/**
 * Implementation of the get motd service.
 *
 * <p>Note that this service must be state-less so that it can be run on
 * multiple servers.
 *
 * @author kerr@google.com (Debby Wallach)
 */
public class GetMotdServiceImpl extends OdeRemoteServiceServlet implements GetMotdService {

  // Logging support
  private static final Logger LOG = Logger.getLogger(GetMotdServiceImpl.class.getName());
  
  // The value of this flag can be changed in appengine-web.xml
  private static final Flag<Integer> motdCheckIntervalSecs = 
    Flag.createFlag("motd.check.interval.secs", 300);

  private final StorageIo storageIo = StorageIoInstanceHolder.INSTANCE;

  /**
   * Gets the current Motd
   *
   * @return  the current Motd
   */
  @Override
  public Motd getMotd() {
    return storageIo.getCurrentMotd();
  }
  
  /**
   * Returns the value, in seconds, of the motd.check.interval.secs flag.
   * 0 means don't check motd.
   */
  @Override
  public int getCheckInterval() {
    return motdCheckIntervalSecs.get();
  }
}
