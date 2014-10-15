// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

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
