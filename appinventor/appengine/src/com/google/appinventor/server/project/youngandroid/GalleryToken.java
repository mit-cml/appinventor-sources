// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2020 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

/**
 * This module is used to generate and validate "tokens" used to
 * securely communicate between the new MIT App Inventor Gallery and a
 * given instance of MIT App Inventor
 *
 * tokens are serialized protocol buffers which are then encrypted
 * with KeyCzar
 *
 * As a hack to keep the tokens smaller, the timestamp uses python
 * timestamps (which are Java timestamps divided by 1000 with January
 * 1, 2020 substracted "Hackito Ergo Sum"
 *
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 */

package com.google.appinventor.server.project.youngandroid;

import com.google.appinventor.server.flags.Flag;

import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;

import org.keyczar.util.Base64Coder;

// Log4j
import org.apache.log4j.Logger;

public class GalleryToken {
  private static long JANONE2020 = 1577836800L; // January 1, 2020 00:00 UTC
  private static final Flag<String> galleryKeyFile = Flag.createFlag("gallery.tokenkey", "WEB-INF/gallerykey");
  private static final Logger LOG = Logger.getLogger(GalleryToken.class);
  private static Crypter crypter = null; // accessed through getCrypter only
  private static final Object crypterSync = new Object();

  private GalleryToken() {      // No instantiation
  }

  public static String makeToken (String userId, long projectId, String projectName) {
    long ts = System.currentTimeMillis() / 1000L;
    ts -= JANONE2020;
    GalleryProtobuf.token buffer = GalleryProtobuf.token.newBuilder()
      .setUserid(userId)
      .setProjectid(projectId)
      .setProjectname(projectName)
      .setTs(ts).build();
    try {
      Crypter crypter = getCrypter();
      return Base64Coder.encode(crypter.encrypt(buffer.toByteArray()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  private static Crypter getCrypter() throws KeyczarException {
    synchronized(crypterSync) {
      if (crypter != null) {
        return crypter;
      } else {
        crypter = new Crypter(galleryKeyFile.get());
        return crypter;
      }
    }
  }
}
