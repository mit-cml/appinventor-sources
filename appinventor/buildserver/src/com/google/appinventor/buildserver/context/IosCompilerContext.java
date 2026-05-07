// -*- mode: java; c-basic-offset: 2; -*-
// Copyright © 2020-2025 Massachusetts Institute of Technology, All rights reserved.
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.buildserver.context;

import com.dd.plist.NSDictionary;
import com.google.appinventor.buildserver.util.EncryptionUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class IosCompilerContext extends CompilerContext<IosPaths> {

  private String teamId = null;
  private String packageId = null;
  private File entitlements = null;
  private NSDictionary adhocProfile = null;
  private NSDictionary appstoreProfile = null;
  private String certificateSha = null;
  private String certificate = null;
  private String appleId = null;
  private String appSpecificPassword = null;
  private String shortName = null;
  private boolean didParseCredentials = false;

  public IosCompilerContext() {
    super(new IosPaths());
  }

  public void setTeamId(String teamId) {
    this.teamId = teamId;
  }

  public String getTeamId() {
    return teamId;
  }

  public void setPackageId(String packageId) {
    this.packageId = packageId;
  }

  public String getBundleId() {
    return packageId;
  }

  public String getAppId() {
    return teamId + "." + packageId;
  }

  public void setEntitlements(File entitlements) {
    this.entitlements = entitlements;
  }

  public File getEntitlements() {
    return entitlements;
  }

  public void setAdhocProfile(NSDictionary profile) {
    adhocProfile = profile;
  }

  public void setAppstoreProfile(NSDictionary profile) {
    appstoreProfile = profile;
  }

  public void setCertificateSignature(String sig) {
    certificateSha = sig;
  }

  public String getCertificateSignature() {
    return certificateSha;
  }

  public void setCertificate(String certificate) {
    this.certificate = certificate;
  }

  public String getCertificate() {
    return certificate;
  }

  public String getAppleId() {
    parseCredentials();
    return appleId;
  }

  public String getAppSpecificPassword() {
    parseCredentials();
    return appSpecificPassword;
  }

  public String getShortName() {
    parseCredentials();
    return shortName;
  }

  public boolean isAdHoc() {
    return adhocProfile != null;
  }

  public boolean isForAppStore() {
    return appstoreProfile != null;
  }

  private void parseCredentials() {
    if (didParseCredentials) {
      return;
    }
    File path = new File(new File(getKeystoreFilePath()).getParentFile(),
        "appstore_credentials.der");
    try (BufferedReader in = new BufferedReader(new InputStreamReader(
        EncryptionUtils.decryptFile(path, new File("buildserver.key"))))) {
      appleId = in.readLine();
      appSpecificPassword = in.readLine();
      shortName = in.readLine();  // Will be null if the user didn't specify
    } catch (IOException e) {
      throw new IllegalStateException("App Store build requested without App Store credentials.", e);
    }
    didParseCredentials = true;
  }
}
