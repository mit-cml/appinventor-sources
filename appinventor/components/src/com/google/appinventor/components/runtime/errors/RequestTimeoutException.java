// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2019 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.components.runtime.errors;

import com.google.appinventor.components.runtime.util.ErrorMessages;

import java.io.IOException;

/**
 * Runtime error indicating that a network request has timed out.
 */
public class RequestTimeoutException extends IOException {
    final int errorNumber;

    public RequestTimeoutException() {
        super();
        this.errorNumber = ErrorMessages.ERROR_WEB_REQUEST_TIMED_OUT;
    }
}
