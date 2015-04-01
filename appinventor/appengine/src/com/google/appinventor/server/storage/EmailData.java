// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

import javax.persistence.Id;

import com.googlecode.objectify.annotation.Indexed;

public class EmailData {

  @Id Long id;
  String senderId;    // the sender id
  String receiverId;  // the receiver id
  String title;       // the email title
  String body;        // the email body
  @Indexed public long datestamp;

}
