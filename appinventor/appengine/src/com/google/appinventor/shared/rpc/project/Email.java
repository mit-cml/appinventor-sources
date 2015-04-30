// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;
/**
 * Message is a shared object. The db puts messages in it and slings it back
 * to the view
 */
public class Email implements IsSerializable{

  @Override
  public String toString() {
    return "Email [id=" + id + ", senderId=" + senderId + ", receiverId="
        + receiverId + ", body=" + body + ", timetamp=" + timetamp + "]";
  }

  private long id;
  private String senderId;
  private String receiverId;
  private String title;
  private String body;
  private long timetamp;

  public static final long NOTRECORDED = 0;

  public static final long NO_LAST_EMAIL_NOTIFICATION_ACTIVITY = 0;

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private Email() {
  }

  public Email(Long id, String senderId, String receiverId, String title,
      String body, long timetamp) {
    super();
    this.id = id;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.title = title;
    this.body = body;
    this.timetamp = timetamp;
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getSenderId() {
    return senderId;
  }

  public void setSenderId(String senderId) {
    this.senderId = senderId;
  }

  public String getReceiverId() {
    return receiverId;
  }

  public void setReceiverId(String receiverId) {
    this.receiverId = receiverId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public long getTimetamp() {
    return timetamp;
  }

  public void setTimetamp(long timetamp) {
    this.timetamp = timetamp;
  }

}
