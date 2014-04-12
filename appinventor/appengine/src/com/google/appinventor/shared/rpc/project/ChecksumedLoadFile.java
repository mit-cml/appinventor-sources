// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2014 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.project;

import java.io.Serializable;
import java.security.MessageDigest;
import java.io.ObjectStreamClass;

/**
 * A class used to return text files, in particular .bky and .scm files
 * from the App Inventor service to the client. This class contains the
 * actual file contents as well as a checksum (SHA1) of the file contents
 * this is computed on the server and checked on the client. The purpose
 * of the check is to detect corruption introduced by server proxies that
 * may exist between the client browser and the App Inventor service.
 * We hypothesize that this servers can at times silently alter the contents
 * of data as it goes through the proxy. Usually this is to filter out
 * objectionable content.
 *
 * We'll see how this works...
 *
 */

public class ChecksumedLoadFile implements Serializable {

  // For serialization
  private static final long serialVersionUID = -6859774089621971672L;

  private String content;
  private String checksum;

  public String getContent() throws ChecksumedFileException {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
      byte [] retval = md.digest(content.getBytes("UTF-8"));
      String hexval = byteArrayToHexString(retval);
      if (!hexval.equals(checksum)) {
        throw new ChecksumedFileException("Checksum doesn't match " + hexval + " " + checksum);
      }
      return content;
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new ChecksumedFileException(e.toString());
    } catch (java.io.UnsupportedEncodingException e) { // We need to catch this because it can
      throw new ChecksumedFileException(e.toString()); // be thrown by the .getBytes() call above
    }
  }

  public void setContent(String content) throws ChecksumedFileException {
    MessageDigest md = null;
    try {
      md = MessageDigest.getInstance("MD5");
      byte [] retval = md.digest(content.getBytes("UTF-8"));
      String hexval = byteArrayToHexString(retval);
      this.checksum = hexval;
      this.content = content;
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new ChecksumedFileException(e.toString());
    } catch (java.io.UnsupportedEncodingException e) {
      throw new ChecksumedFileException(e.toString());
    }
  }

  private static String byteArrayToHexString(byte[] b) {
    String result = "";
    for (int i=0; i < b.length; i++) {
      result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
    }
    return result;
  }
}