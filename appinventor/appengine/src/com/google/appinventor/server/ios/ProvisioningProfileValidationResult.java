// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.ios;

/**
 * Enum to represent the result of validating a provisioning profile.
 */
public enum ProvisioningProfileValidationResult {
  /**
   * The provisioning profile is valid.
   */
  SUCCESS,

  /**
   * The provisioning profile is not an ad-hoc profile.
   */
  NO_ADHOC_PROFILE,

  /**
   * The project does not contain an App Store provisioning profile.
   */
  NO_APPSTORE_PROFILE,

  /**
   * The certificate in the provisioning profile is expired.
   */
  EXPIRED_CERTIFICATE,

  /**
   * The provisioning profile is expired.
   */
  EXPIRED_PROFILE,

  /**
   * The provisioning profile is missing a certificate.
   */
  MISSING_CERTIFICATE
}
