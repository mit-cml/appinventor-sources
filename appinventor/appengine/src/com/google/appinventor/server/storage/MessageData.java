package com.google.appinventor.server.storage;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Indexed;

public class MessageData {

  @Id Long id;
  String senderId;
  String receiverId;
  String message;   // the message body
  String status; // 1 = notify; 2 = active; 3 = removed
  @Indexed public long datestamp;

}
