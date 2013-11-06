// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc;

/**
 * Data Transfer Object representing motd.
 *
 * @author kerr@google.com (Debby Wallach)
 */
public interface MotdProvider {

  /**
   * Returns the motd object.
   *
   * @return motd object
   */
  Motd getMotd();

  /**
   * Returns the id.
   *
   * @return id
   */
  public long getId();

  /**
   * Returns the caption.
   *
   * @return caption
   */
  public String getCaption();

  /**
   * Returns the content.
   *
   * @return content
   */
  public String getContent();
}
