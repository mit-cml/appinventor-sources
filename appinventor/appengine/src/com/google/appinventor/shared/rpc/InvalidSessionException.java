// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2009-2011 Google, All Rights reserved
// Copyright 2011-2012 MIT, All rights reserved
// Released under the MIT License https://raw.github.com/mit-cml/app-inventor/master/mitlicense.txt

package com.google.appinventor.shared.rpc;

import java.io.Serializable;

public class InvalidSessionException extends Exception implements Serializable {

    private String message;

    public InvalidSessionException() {
      super();
    }

    public InvalidSessionException(String message) {
      super();
      this.message = message;
    }

    public String getMessage() {
      return message;
    }

}
