// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2015 MIT, All rights reserved
// This is unreleased code.

package com.google.appinventor.server.util;

public class LicenseConfig {
  private String hardwarehint;
  private String UUID;
  private String authCode;

  public LicenseConfig(String hardwarehint, String UUID, String authCode) {
    this.UUID = UUID;
    this.hardwarehint = hardwarehint;
    this.authCode = authCode;
  }

  public String getHardwareHint() {
    return hardwarehint;
  }

  public String getUUID() {
    return UUID;
  }

  public String getAuthCode() {
    return authCode;
  }

  public void setAuthCode(String authCode) {
    this.authCode = authCode;
  }

}
