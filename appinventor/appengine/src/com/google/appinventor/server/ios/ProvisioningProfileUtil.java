// -*- mode: java; c-basic-offset: 2; -*-
// Copyright 2024-2025 MIT, All rights reserved
// Released under the Apache License, Version 2.0
// http://www.apache.org/licenses/LICENSE-2.0

package com.google.appinventor.server.ios;

import com.dd.plist.NSArray;
import com.dd.plist.NSData;
import com.dd.plist.NSDate;
import com.dd.plist.NSDictionary;
import com.dd.plist.PropertyListParser;
import com.google.appinventor.server.storage.StorageIo;
import com.google.appinventor.server.storage.StorageIoInstanceHolder;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import org.bouncycastle.cms.CMSSignedData;

/**
 * Utility class for processing Apple provisioning profiles.
 *
 * @author ewpatton@mit.edu (Evan W. Patton)
 */
public class ProvisioningProfileUtil {
  /**
   * Validates the provisioning profile(s) included in the project assets and determines whether
   * at least one of the profiles is valid for the given build type.
   *
   * @param userId the user ID of the project owner
   * @param projectId the project ID
   * @param forAppStore whether the user requested an App Store build or an ad hoc build
   * @return SUCCESS if the provisioning profile is valid, otherwise an error code from
   *     {@link ProvisioningProfileValidationResult}
   */
  public static ProvisioningProfileValidationResult validateProvisioningProfile(String userId,
      long projectId, boolean forAppStore) {
    StorageIo storageIo = StorageIoInstanceHolder.getInstance();
    List<String> files = storageIo.getProjectSourceFiles(userId, projectId);
    for (String file : files) {
      if (!file.endsWith(".mobileprovision")) {
        continue;
      }
      try {
        CMSSignedData data = new CMSSignedData(storageIo.downloadRawFile(userId, projectId, file));
        byte[] content = (byte[]) data.getSignedContent().getContent();
        NSDictionary profile = (NSDictionary) PropertyListParser.parse(content);
        NSDate expirationDate = (NSDate) profile.get("ExpirationDate");
        if (expirationDate.getDate().before(new Date())) {
          return ProvisioningProfileValidationResult.EXPIRED_PROFILE;
        }
        boolean adhoc = !forAppStore;
        boolean provisioned = profile.containsKey("ProvisionedDevices");
        if ((adhoc && provisioned) || (!adhoc && !provisioned)) {
          X509Certificate cert = getCertificateFromProfile(profile);
          if (cert == null) {
            return ProvisioningProfileValidationResult.MISSING_CERTIFICATE;
          } else if (cert.getNotAfter().before(new Date())) {
            return ProvisioningProfileValidationResult.EXPIRED_CERTIFICATE;
          } else {
            return ProvisioningProfileValidationResult.SUCCESS;
          }
        }
      } catch (Exception e) {
        throw new IllegalStateException("Unable to parse provisioning profile", e);
      }
    }
    if (forAppStore) {
      return ProvisioningProfileValidationResult.NO_APPSTORE_PROFILE;
    } else {
      return ProvisioningProfileValidationResult.NO_ADHOC_PROFILE;
    }
  }

  public static NSDictionary decodeProfile(byte[] profileBytes) {
    try {
      CMSSignedData data = new CMSSignedData(profileBytes);
      byte[] content = (byte[]) data.getSignedContent().getContent();
      return (NSDictionary) PropertyListParser.parse(content);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to parse provisioning profile", e);
    }
  }

  /**
   * Extracts the X.509 certificate from the provisioning profile.
   *
   * @param profile the parsed provisioning profile
   * @return the X.509 certificate, or null if the profile does not contain a certificate
   * @throws CertificateException if the certificate cannot be parsed
   */
  public static X509Certificate getCertificateFromProfile(NSDictionary profile)
      throws CertificateException {
    NSArray certificates = (NSArray) profile.get("DeveloperCertificates");
    if (certificates.count() == 0) {
      return null;
    }
    String pemCert = ((NSData) certificates.objectAtIndex(0)).getBase64EncodedData();
    pemCert = "-----BEGIN CERTIFICATE-----\n" + pemCert + "\n-----END CERTIFICATE-----\n";
    CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
    return (X509Certificate) certificateFactory.generateCertificate(
        new ByteArrayInputStream(pemCert.getBytes()));
  }
}
