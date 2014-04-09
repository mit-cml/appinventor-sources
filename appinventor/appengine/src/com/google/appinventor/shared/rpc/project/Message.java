package com.google.appinventor.shared.rpc.project;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.logging.Logger;
/**
 * Message is a shared object. The db puts messages in it and slings it back
 * to the view
 */
public class Message implements IsSerializable{
  @Override
  public String toString() {
    return "Message [senderId=" + senderId + ", receiverId=" + receiverId
        + ", message=" + message + ", status=" + status + ", timetamp="
        + timetamp + "]";
  }


  private String senderId; // 0 = system sent
  private String receiverId;
  private String message;
  private String status; // 1 = notify; 2 = active; 3 = removed
  private long timetamp;

  /**
   * Default constructor. This constructor is required by GWT.
   */
  @SuppressWarnings("unused")
  private Message() {
  }

  public Message(String senderId, String receiverId, String message,
      String status, long timetamp) {
    super();
    this.senderId = senderId;
    this.receiverId = receiverId;
    this.message = message;
    this.status = status;
    this.timetamp = timetamp;
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


}
