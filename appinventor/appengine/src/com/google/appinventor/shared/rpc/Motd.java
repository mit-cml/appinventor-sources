// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.io.Serializable;

/**
 * Data Transfer Object representing motd.
 *
 * @author kerr@google.com (Debby Wallach)
 */
public class Motd implements IsSerializable, MotdProvider, Serializable {
  private long id;

  // Caption of the MOTD
  private String caption;

  // The content of the MOTD, if there is any
  private String content = null;

  /**
   * Creates a new motd data transfer object.
   *
   * @param id  motd's ID
   * @param caption  caption
   * @param content  more detail, if any
   */
  public Motd(long id, String caption, String content) {
    this.id = id;
    this.caption = caption;
    this.content = content;
  }

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private Motd() {
  }

  /**
   * Returns the id.
   *
   * @return id
   */
  public long getId() {
    return id;
  }

  /**
   * Returns the caption.
   *
   * @return caption
   */
  public String getCaption() {
    return caption;
  }

  /**
   * Returns whether or not the MOTD has content
   */
  public boolean hasContent() {
    return content != null;
  }

  /**
   * Returns the content.
   *
   * @return content
   */
  public String getContent() {
    return content;
  }
  /**
   * Returns this object.
   *
   * @return motd
   */
  @Override
  public Motd getMotd() {
    return this;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Motd)) return false;
    Motd motd = (Motd) obj;
    if (!motd.getCaption().equals(this.caption)) return false;
    if (!(motd.hasContent() == this.hasContent())) return false;
    return motd.hasContent() ? motd.getContent().equals(this.content) : true;
  }

  @Override
  public int hashCode() {
    return (int) (id ^ (id >>> 32));
  }
}
