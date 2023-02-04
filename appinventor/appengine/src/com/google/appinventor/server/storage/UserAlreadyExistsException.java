// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2016 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.storage;

/**
 * @author jis@mit.edu (Jeffrey I. Schiller)
 *
 */
public class UserAlreadyExistsException extends Exception {

 public UserAlreadyExistsException(String msg) {
   super(msg);
 }
}

