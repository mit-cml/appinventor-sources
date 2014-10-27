// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.shared.rpc.project;

import com.google.gwt.user.client.rpc.IsSerializable;
/**
 * Message is a shared object. The db puts messages in it and slings it back
 * to the view
 */
public class Message implements IsSerializable{

  @Override
  public String toString() {
    return "Message [id=" + id + ", senderId=" + senderId + ", receiverId="
        + receiverId + ", message=" + message + ", status=" + status
        + ", timetamp=" + timetamp + "]";
  }

  private long id;
  private String senderId; // 0 = system sent
  private String receiverId;
  private String message;
  private String status; // 1 = unread; 2 = read; 3 = removed
  private long timetamp;

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private Message() {
  }

  public Message(Long id, String senderId, String receiverId, String message,
      String status, long timetamp) {
    super();
    this.id = id;
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.message = message;
    this.status = status;
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

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public long getTimetamp() {
    return timetamp;
  }

  public void setTimetamp(long timetamp) {
    this.timetamp = timetamp;
  }

  public boolean isUnread() {
    if (this.status.equalsIgnoreCase("1"))
      return true;
    else
      return false;
  }

}
